package client.scenes;

import commons.Recipe;
import com.google.inject.Inject;
import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

import java.util.ArrayList;

public class FoodPalMainCtrl {

    private final ServerUtils server;

    @FXML
    private ListView<Recipe> colRecipeList;

    private ObservableList<Recipe> data;

    @Inject
    public FoodPalMainCtrl(ServerUtils server) {
        this.server = server;
    }

    @FXML
    public void initialize() {
        data = FXCollections.observableArrayList();
        colRecipeList.setItems(data);
        refreshRecipes();
    }

    @FXML
    public void refreshRecipes() {
        var recipes = server.getRecipes();   // GET /api/recipes
        data.setAll(recipes);
    }

    @FXML
    private void addRecipe() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add recipe");
        dialog.setHeaderText(null);
        dialog.setContentText("Recipe name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                var recipe = new Recipe(name, new ArrayList<>(), new ArrayList<>());

                var saved = server.addRecipe(recipe);

                data.add(saved);
            }
        });
    }
}
