package client.scenes;

import commons.RecipeIngredient;
import commons.Step;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import commons.Recipe;
import java.util.ArrayList;

import com.google.inject.Inject;
import client.utils.ServerUtils;
import javafx.scene.layout.VBox;

public class FoodPalMainCtrl {

    private final ServerUtils server;

    @FXML
    private VBox detailPane;

    @FXML
    private Label recipeTitle;

    @FXML
    private VBox ingredientsBox;

    @FXML
    private VBox stepsBox;


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

    public void showRecipe(Recipe recipe) {
        //recipe title
        recipeTitle.setText(recipe.getName());

        //show ingredients
        ingredientsBox.getChildren().clear();
        if(recipe.getIngredients().isEmpty()){
            ingredientsBox.getChildren().add(new Label("No ingredients found"));
        }
        else{
            for(RecipeIngredient ingre : recipe.getIngredients()){
                ingredientsBox.getChildren()
                        .add(new Label(ingre.getAmount() + " "
                                + ingre.getUnit() + " "
                                + ingre.getIngredient().getName()));
            }
        }

        //show preparation
        stepsBox.getChildren().clear();
        if(recipe.getSteps().isEmpty()){
            stepsBox.getChildren().add(new Label("No steps found"));
        }
        else{
            for(Step step : recipe.getSteps()){
                stepsBox.getChildren().add(new Label(step.getText()));
            }
        }

    }



    public void refresh() {
        var recipes = server.getRecipes();              // GET /api/recipes
        var data = FXCollections.observableList(recipes);
        colRecipeList.setItems(data);                   // ListView updates automatically
    }

    public void initialize() {
        colRecipeList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldRecipe, newRecipe)
                        -> {if(newRecipe != null){
                            showRecipe(newRecipe);
                        }
                }
                );
    }
}
