package client.scenes;

import client.utils.ConfigService;
import client.utils.ServerUtils;
import client.utils.WebSocketService;

import commons.Recipe;
import commons.RecipeEvent;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompSession;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

public class FoodPalMainCtrlTests {

    private FoodPalMainCtrl sut;
    private StubServerUtils server;
    private StubWebSocketService websocket;
    private ConfigService config; // Promoted to field so we can add favorites in tests

    // UI Components
    private ListView<Recipe> colRecipeList;
    private ToggleButton allRecipesButton;
    private ToggleButton favouriteRecipesButton;
    private ToggleGroup filterGroup;
    private TextField searchField;

    @BeforeAll
    static void initJavaFX() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux") && System.getenv("DISPLAY") == null) {
            Assumptions.assumeTrue(false, "Skipping UI tests in headless CI environment");
            return;
        }

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Mocks & Stubs
        server = new StubServerUtils();
        websocket = new StubWebSocketService();

        File temp = File.createTempFile("foodpal_test_config", ".json");
        temp.deleteOnExit();
        config = new ConfigService(temp);

        sut = new FoodPalMainCtrl(server, websocket, config);

        // 2. Initialize UI Components
        colRecipeList = new ListView<>();
        allRecipesButton = new ToggleButton();
        favouriteRecipesButton = new ToggleButton();
        filterGroup = new ToggleGroup();
        searchField = new TextField();

        // Configure Toggle Logic (JavaFX behavior)
        allRecipesButton.setToggleGroup(filterGroup);
        favouriteRecipesButton.setToggleGroup(filterGroup);
        allRecipesButton.setSelected(true); // Default state

        ObservableList<Recipe> initialData = FXCollections.observableArrayList();
        colRecipeList.setItems(initialData); // Bind list to view

        // 3. Inject Private Fields
        setPrivateField(sut, "colRecipeList", colRecipeList);
        setPrivateField(sut, "recipeTitle", new Label());
        setPrivateField(sut, "ingredientsList", new ListView<>());
        setPrivateField(sut, "stepsBox", new VBox());
        setPrivateField(sut, "data", initialData);
        setPrivateField(sut, "contentPane", new BorderPane());

        // Inject the new Filtering Components
        setPrivateField(sut, "allRecipesButton", allRecipesButton);
        setPrivateField(sut, "favouriteRecipesButton", favouriteRecipesButton);
        setPrivateField(sut, "filterGroup", filterGroup);
        setPrivateField(sut, "searchField", searchField);
    }

    // --- TESTS: WebSocket Updates ---

    @Test
    void testInitializeSubscribesToWebsockets() {
        websocket.connected = true;
        sut.initialize();

        assertEquals("/topic/recipes", websocket.lastTopic);
        assertEquals(RecipeEvent.class, websocket.lastType);
        assertNotNull(websocket.lastCallback, "Callback should have been registered");
    }

    @Test
    void testHandleServerEvent_ADD() throws InterruptedException {
        websocket.connected = true;
        sut.initialize();
        Consumer<RecipeEvent> callback = websocket.lastCallback;

        // Simulate server having the new recipe
        server.setRecipes(createRecipe(101L, "New Recipe"));

        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.ADD, 101L, "New Recipe");
        callback.accept(event);
        waitForRunLater();

        ObservableList<Recipe> currentList = colRecipeList.getItems();
        assertEquals(1, currentList.size());
        assertEquals(101L, currentList.get(0).getId());
    }

    @Test
    void testHandleServerEvent_DELETE() throws InterruptedException {
        websocket.connected = true;
        sut.initialize();

        Recipe r1 = createRecipe(50L, "To Delete");
        server.setRecipes(r1);

        // Refresh to populate list initially
        sut.refreshRecipes();
        assertEquals(1, colRecipeList.getItems().size());

        // Simulate item deleted from server
        server.setRecipes(); // Empty

        Consumer<RecipeEvent> callback = websocket.lastCallback;
        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.DELETE, 50L, null);
        callback.accept(event);
        waitForRunLater();

        assertTrue(colRecipeList.getItems().isEmpty());
    }

    @Test
    void testHandleServerEvent_UPDATE() throws InterruptedException {
        websocket.connected = true;
        sut.initialize();

        Recipe r1 = createRecipe(60L, "Old Name");
        server.setRecipes(r1);
        sut.refreshRecipes();

        Consumer<RecipeEvent> callback = websocket.lastCallback;

        // Simulate server update
        server.setRecipes(createRecipe(60L, "New Name"));

        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.UPDATE, 60L, "New Name");
        callback.accept(event);
        waitForRunLater();

        ObservableList<Recipe> currentList = colRecipeList.getItems();
        assertEquals(1, currentList.size());
        assertEquals("New Name", currentList.get(0).getName());
    }

    // --- TESTS: Filtering (Buttons) ---

    @Test
    void testRefreshRecipes_ShowsAll_WhenAllSelected() {
        // Arrange
        server.setRecipes(
                createRecipe(1L, "Pizza"),
                createRecipe(2L, "Soup")
        );
        allRecipesButton.setSelected(true);
        favouriteRecipesButton.setSelected(false);

        // Act
        sut.refreshRecipes();

        // Assert
        assertEquals(2, colRecipeList.getItems().size());
    }

    @Test
    void testRefreshRecipes_ShowsOnlyFavorites_WhenFavSelected() {
        // Arrange
        server.setRecipes(
                createRecipe(1L, "Pizza"), // Will be fav
                createRecipe(2L, "Soup")   // Not fav
        );

        // Mark 1 as favorite
        config.addFavourite(1L);

        // Select Favorite Button
        allRecipesButton.setSelected(false);
        favouriteRecipesButton.setSelected(true);

        // Act
        sut.refreshRecipes();

        // Assert
        assertEquals(1, colRecipeList.getItems().size());
        assertEquals("Pizza", colRecipeList.getItems().get(0).getName());
    }

    @Test
    void testRefreshRecipes_SearchAndFavoriteCombination() {
        // Arrange
        server.setRecipes(
                createRecipe(1L, "Pizza Margherita"), // Fav + Match
                createRecipe(2L, "Pizza Pepperoni"),  // Not Fav + Match
                createRecipe(3L, "Tomato Soup")       // Fav + No Match
        );

        config.addFavourite(1L);
        config.addFavourite(3L);

        // Select Favorite Button
        favouriteRecipesButton.setSelected(true);
        // Enter Search Text
        searchField.setText("Pizza");

        // Act
        sut.refreshRecipes();

        // Assert
        // Should only show items that are BOTH favorites AND contain "Pizza"
        assertEquals(1, colRecipeList.getItems().size());
        assertEquals("Pizza Margherita", colRecipeList.getItems().get(0).getName());
    }

    // --- HELPER CLASSES ---

    private Recipe createRecipe(Long id, String name) {
        Recipe r = new Recipe(name, new ArrayList<>(), new ArrayList<>());
        r.setId(id);
        return r;
    }

    static class StubServerUtils extends ServerUtils {
        private List<Recipe> recipes = new ArrayList<>();

        public void setRecipes(Recipe... newRecipes) {
            this.recipes = new ArrayList<>(List.of(newRecipes));
        }

        public void setRecipes(List<Recipe> newRecipes) {
            this.recipes = new ArrayList<>(newRecipes);
        }

        @Override
        public List<Recipe> getRecipes() {
            // Return copy
            return new ArrayList<>(recipes);
        }
    }

    static class StubWebSocketService extends WebSocketService {
        boolean connected = false;
        String lastTopic;
        Class<?> lastType;
        Consumer<RecipeEvent> lastCallback;

        @Override
        public boolean isConnected() {
            return connected;
        }

        @Override
        public void setConnectionListener(ConnectionListener listener) {
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> StompSession.Subscription subscribe(String topic, Class<T> type, Consumer<T> callback) {
            this.lastTopic = topic;
            this.lastType = type;
            this.lastCallback = (Consumer<RecipeEvent>) callback;
            return null;
        }
    }

    // --- HELPER METHODS ---

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void waitForRunLater() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        assertTrue(latch.await(2, TimeUnit.SECONDS), "Timeout waiting for Platform.runLater");
    }
}