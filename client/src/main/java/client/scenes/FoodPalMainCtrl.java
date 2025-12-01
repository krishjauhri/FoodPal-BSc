package client.scenes;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;

import com.google.inject.Inject;
import client.utils.ServerUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;

import java.util.ArrayList;


public class FoodPalMainCtrl {

    private final ServerUtils server;

    @FXML
    private ListView<Recipe> colRecipeList;

    @FXML
    private VBox detailPane;

    @FXML
    private Label recipeTitle;

    @FXML
    private VBox ingredientsBox;

    @FXML
    private VBox stepsBox;

    private ObservableList<Recipe> data;

    @Inject
    public FoodPalMainCtrl(ServerUtils server) {
        this.server = server;
    }

    @FXML
    public void initialize() {
        // lijst initialiseren
        data = FXCollections.observableArrayList();
        colRecipeList.setItems(data);
        refreshRecipes();

        // listener voor detailscherm
        colRecipeList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldRecipe, newRecipe) -> {
                    if (newRecipe != null) {
                        showRecipe(newRecipe);
                    }
                });
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

                // voeg nieuwe recept toe aan de observable list
                data.add(saved);
            }
        });
    }

    public void showRecipe(Recipe recipe) {
        // title
        recipeTitle.setText(recipe.getName());

        // ingredients
        ingredientsBox.getChildren().clear();
        if (recipe.getIngredients().isEmpty()) {
            ingredientsBox.getChildren().add(new Label("No ingredients found"));
        } else {
            for (RecipeIngredient ingre : recipe.getIngredients()) {
                ingredientsBox.getChildren()
                        .add(new Label(ingre.getAmount() + " "
                                + ingre.getUnit() + " "
                                + ingre.getIngredient().getName()));
            }
        }

        // steps
        stepsBox.getChildren().clear();
        if (recipe.getSteps().isEmpty()) {
            stepsBox.getChildren().add(new Label("No steps found"));
        } else {
            for (Step step : recipe.getSteps()) {
                stepsBox.getChildren().add(new Label(step.getText()));
            }
        }
    }
}

