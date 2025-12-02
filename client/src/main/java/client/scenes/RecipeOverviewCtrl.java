package client.scenes;

import client.utils.ServerUtils;
import commons.Recipe;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;

public class RecipeOverviewCtrl {

    private final ServerUtils server;

    @FXML
    private ListView<Recipe> recipeList;

    private ObservableList<Recipe> data;

    @Inject
    public RecipeOverviewCtrl(ServerUtils server) {
        this.server = server;
    }

    @FXML
    public void initialize() {
        data = FXCollections.observableArrayList();
        recipeList.setItems(data);
        refreshRecipes();
    }

    @FXML
    public void refreshRecipes() {
        var recipes = server.getRecipes();
        data.setAll(recipes);
    }
}
