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

    @FXML
    private ListView<RecipeIngredient> ingredientsList;

    @FXML
    private void addIngredient() {
        Recipe recipe = colRecipeList.getSelectionModel().getSelectedItem();
        if (recipe == null) return; // no recipe selected

        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Add Ingredient");
        nameDialog.setHeaderText("Enter ingredient name:");
        nameDialog.setContentText("Name:");
        var nameResult = nameDialog.showAndWait();
        if (nameResult.isEmpty()) return;
        String name = nameResult.get();

        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Amount");
        amountDialog.setHeaderText("Enter amount:");
        amountDialog.setContentText("Amount (Example: 200):");
        var amountResult = amountDialog.showAndWait();
        if (amountResult.isEmpty()) return;
        double amount = Double.parseDouble(amountResult.get());

        TextInputDialog unitDialog = new TextInputDialog();
        unitDialog.setTitle("Unit");
        unitDialog.setHeaderText("Enter unit:");
        unitDialog.setContentText("Unit (g, ml, pcs, ...):");
        var unitResult = unitDialog.showAndWait();
        if (unitResult.isEmpty()) return;
        String unit = unitResult.get();

        // Send to backend
        var ingredient = server.createIngredient(name, 0, 0, 0); // nutrition later
        server.addIngredient(recipe.getId(), ingredient.getId(), amount, unit);

        refresh(); // reload UI
    }


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
        showIngredients(recipe);
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

    private void showIngredients(Recipe recipe) {
        ingredientsList.getItems().setAll(
                recipe.getIngredients().stream()
                        .filter(i -> i.getIngredient() != null)
                        .filter(i -> i.getUnit() != null)
                        .filter(i -> i.getAmount() > 0)
                        .toList()
        );
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
