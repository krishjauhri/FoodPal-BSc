package client.scenes;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import commons.Recipe;
import java.util.ArrayList;

import com.google.inject.Inject;
import client.utils.ServerUtils;

public class FoodPalMainCtrl {

    private final ServerUtils server;

    @Inject
    public FoodPalMainCtrl(ServerUtils server) {
        this.server = server;
    }

    @FXML
    private ListView<Recipe> colRecipeList;

    @FXML
    private void addRecipe() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add recipe");
        dialog.setHeaderText(null);
        dialog.setContentText("Recipe name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                var recipe = new Recipe(name, new ArrayList<>(), new ArrayList<>());

                // send to server → DB
                var saved = server.addRecipe(recipe);

                // add the saved recipe (has id from DB) to the list
                colRecipeList.getItems().add(saved);
            }
        });
    }

    public void refresh() {
        var recipes = server.getRecipes();              // GET /api/recipes
        var data = FXCollections.observableList(recipes);
        colRecipeList.setItems(data);                   // ListView updates automatically
    }

    public void initialize() {}
}
