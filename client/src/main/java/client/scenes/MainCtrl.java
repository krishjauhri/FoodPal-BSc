package client.scenes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class MainCtrl {

    private Stage primaryStage;

    private FoodPalMainCtrl overviewCtrl;
    private Scene overview;

    private AddQuoteCtrl addCtrl;
    private Scene add;

    private void applyStyles(Scene scene) {
        var css = getClass().getResource("/styles/foodpal.css");
        if (css == null) {
            System.err.println("Could not load /styles/foodpal.css (check resources path)");
            return;
        }
        scene.getStylesheets().add(css.toExternalForm());
    }

    public void initialize(Stage primaryStage, Pair<FoodPalMainCtrl, Parent> overview,
                           Pair<AddQuoteCtrl, Parent> add) {
        this.primaryStage = primaryStage;
        this.overviewCtrl = overview.getKey();
        this.overview = new Scene(overview.getValue());
        applyStyles(this.overview);

        this.addCtrl = add.getKey();
        this.add = new Scene(add.getValue());
        applyStyles(this.add);

        showOverview();
        primaryStage.show();
    }

    public void showOverview() {
        primaryStage.setTitle("Recipes: Overview");
        primaryStage.setScene(overview);
        overviewCtrl.refreshRecipes();
    }

    public void showAdd() {
        primaryStage.setTitle("Quotes: Adding Quote");
        primaryStage.setScene(add);
        add.setOnKeyPressed(e -> addCtrl.keyPressed(e));
    }
}
