package client.scenes;

import client.utils.ServerUtils;
import client.utils.WebSocketService;
import commons.Ingredient;
import commons.Recipe;
import commons.RecipeEvent;
import commons.RecipeIngredient;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompSession;

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

    private ListView<Recipe> colRecipeList;

    // 1. Initialize JavaFX Toolkit Safely for CI
    @BeforeAll
    static void initJavaFX() {
        // PRE-CHECK: If we are on Linux (CI runner) and no DISPLAY is set, skip immediately.
        // This prevents the JVM from crashing with UnsatisfiedLinkError before we can catch it.
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux") && System.getenv("DISPLAY") == null) {
            System.out.println("Headless Linux environment detected. Skipping JavaFX tests.");
            Assumptions.assumeTrue(false, "Skipping UI tests in headless CI environment");
            return;
        }

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized - this is expected in some environments
        } catch (Throwable e) {
            // Catch Exception AND Errors (like UnsatisfiedLinkError)
            System.err.println("JavaFX failed to start: " + e.getMessage());
            // Skip the test class if FX cannot start
            Assumptions.assumeTrue(false, "Skipping UI tests: JavaFX Toolkit failed to start.");
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        server = new StubServerUtils();
        websocket = new StubWebSocketService();
        sut = new FoodPalMainCtrl(server, websocket);

        // Inject UI components
        // We initialize these here. If initJavaFX above skipped the tests, this code won't matter.
        colRecipeList = new ListView<>();
        ObservableList<Recipe> initialData = FXCollections.observableArrayList();

        setPrivateField(sut, "colRecipeList", colRecipeList);
        setPrivateField(sut, "recipeTitle", new Label());
        setPrivateField(sut, "ingredientsList", new ListView<>());
        setPrivateField(sut, "stepsBox", new VBox());
        setPrivateField(sut, "data", initialData);

        BorderPane contentPane = new BorderPane();
        contentPane.setCenter(new VBox());
        setPrivateField(sut, "contentPane", contentPane);
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

        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.ADD, 101L, "New Recipe");
        callback.accept(event);
        waitForRunLater();

        ObservableList<Recipe> currentList = colRecipeList.getItems();
        assertEquals(1, currentList.size());
        assertEquals(101L, currentList.get(0).getId());
        assertEquals("New Recipe", currentList.get(0).getName());
    }

    @Test
    void testHandleServerEvent_DELETE() throws InterruptedException {
        websocket.connected = true;
        sut.initialize();

        Recipe r1 = new Recipe("To Delete", new ArrayList<>(), new ArrayList<>());
        r1.setId(50L);
        colRecipeList.getItems().add(r1);

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

        Recipe r1 = new Recipe("Old Name", new ArrayList<>(), new ArrayList<>());
        r1.setId(60L);
        colRecipeList.getItems().add(r1);

        Consumer<RecipeEvent> callback = websocket.lastCallback;

        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.UPDATE, 60L, "New Name");
        callback.accept(event);
        waitForRunLater();

        ObservableList<Recipe> currentList = colRecipeList.getItems();
        assertEquals(1, currentList.size());
        assertEquals("New Name", currentList.get(0).getName());
    }

    // --- HELPER CLASSES ---

    static class StubServerUtils extends ServerUtils {
        @Override
        public List<Recipe> getRecipes() {
            return new ArrayList<>();
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

    // --- UTILS TESTS ---

    @Test
    void extractIngredients() {
        Ingredient egg = new Ingredient("Egg", 13, 11, 1);
        Ingredient milk = new Ingredient("Milk", 3.4, 3.6, 5);
        RecipeIngredient r1i1 = new RecipeIngredient(egg, 1, "pcs");
        RecipeIngredient r1i2 = new RecipeIngredient(milk, 100, "ml");
        RecipeIngredient r2i1 = new RecipeIngredient(egg, 2, "pcs");
        Recipe r1 = new Recipe("R1", List.of(r1i1, r1i2), List.of());
        Recipe r2 = new Recipe("R2", List.of(r2i1), List.of());

        List<Ingredient> result = sut.extractIngredients(List.of(r1, r2));
        assertEquals(2, result.size());
    }

    @Test
    void extractIngredientsFromEmptyRecipes() {
        assertTrue(sut.extractIngredients(List.of()).isEmpty());
    }
}