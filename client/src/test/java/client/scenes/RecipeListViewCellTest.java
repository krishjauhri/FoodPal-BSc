package client.scenes;

import client.utils.ConfigService;
import commons.Recipe;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/// This class was generated using AI
public class RecipeListViewCellTest {

    // Initialize JavaFX Toolkit once for the test class
    @BeforeAll
    static void initJFX() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void testFavoriteColorIsGold() throws InterruptedException {
        // Arrange: Create a stub that always returns TRUE for favorites
        StubConfigService stubConfig = new StubConfigService();
        stubConfig.setFavoriteResponse(true);

        Recipe recipe = new Recipe("Pasta", null, null);
        recipe.setId(10L);

        RecipeListViewCell cell = new RecipeListViewCell(stubConfig);

        // Act: Must run on JavaFX thread
        runOnJFX(() -> cell.updateItem(recipe, false));

        // Assert: Check the UI properties
        HBox graphic = (HBox) cell.getGraphic();
        Button star = (Button) graphic.getChildren().get(2); // 3rd element in HBox

        assertTrue(star.getStyle().contains("gold"), "Style should contain 'gold' for favorites");
        assertEquals("★", star.getText());
    }

    @Test
    void testNonFavoriteColorIsGray() throws InterruptedException {
        // Arrange: Create a stub that always returns FALSE for favorites
        StubConfigService stubConfig = new StubConfigService();
        stubConfig.setFavoriteResponse(false);

        Recipe recipe = new Recipe("Salad", null, null);
        recipe.setId(11L);

        RecipeListViewCell cell = new RecipeListViewCell(stubConfig);

        // Act
        runOnJFX(() -> cell.updateItem(recipe, false));

        // Assert
        HBox graphic = (HBox) cell.getGraphic();
        Button star = (Button) graphic.getChildren().get(2);

        assertTrue(star.getStyle().contains("gray"), "Style should contain 'gray' for non-favorites");
        assertEquals("★", star.getText());
    }

    /**
     * Helper to run code on the JavaFX Thread and wait for it to finish.
     */
    private void runOnJFX(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    /**
     * A Manual Stub for ConfigService.
     * We override the methods so the test doesn't try to read actual JSON files.
     */
    private static class StubConfigService extends ConfigService {
        private boolean response = false;

        public void setFavoriteResponse(boolean value) {
            this.response = value;
        }

        @Override
        public boolean isFavourite(long id) {
            return response;
        }

        @Override
        public boolean toggleFavorite(long id) {
            this.response = !this.response;
            return this.response;
        }
    }
}