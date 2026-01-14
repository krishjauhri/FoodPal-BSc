package client.scenes;

import client.utils.ConfigService;
import commons.Recipe;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

///  This class was generated using AI
public class RecipeListViewCellTest {

    private static boolean isHeadless = false;

    @BeforeAll
    static void initJFX() {
        // Check if we are in a headless environment (like GitLab CI)
        // Macs always have a display, so we check for Linux + no DISPLAY variable
        if (System.getenv("DISPLAY") == null && System.getProperty("os.name").toLowerCase().contains("linux")) {
            System.out.println("Headless Linux environment detected. Skipping JavaFX tests.");
            isHeadless = true;
            return;
        }

        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void testFavoriteColorIsGold() throws InterruptedException {
        // Skip this test if there is no screen available
        Assumptions.assumeFalse(isHeadless);

        StubConfigService stubConfig = new StubConfigService();
        stubConfig.setFavoriteResponse(true);

        Recipe recipe = new Recipe("Pasta", null, null);
        recipe.setId(10L);

        RecipeListViewCell cell = new RecipeListViewCell(stubConfig);

        runOnJFX(() -> cell.updateItem(recipe, false));

        HBox graphic = (HBox) cell.getGraphic();
        Button star = (Button) graphic.getChildren().get(2);

        assertTrue(star.getStyle().contains("gold"));
    }

    @Test
    void testNonFavoriteColorIsGray() throws InterruptedException {
        // Skip this test if there is no screen available
        Assumptions.assumeFalse(isHeadless);

        StubConfigService stubConfig = new StubConfigService();
        stubConfig.setFavoriteResponse(false);

        Recipe recipe = new Recipe("Salad", null, null);
        recipe.setId(11L);

        RecipeListViewCell cell = new RecipeListViewCell(stubConfig);

        runOnJFX(() -> cell.updateItem(recipe, false));

        HBox graphic = (HBox) cell.getGraphic();
        Button star = (Button) graphic.getChildren().get(2);

        assertTrue(star.getStyle().contains("gray"));
    }

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

    private static class StubConfigService extends ConfigService {
        private boolean response = false;
        public void setFavoriteResponse(boolean value) { this.response = value; }
        @Override public boolean isFavourite(long id) { return response; }
        @Override public boolean toggleFavorite(long id) { return true; }
    }
}