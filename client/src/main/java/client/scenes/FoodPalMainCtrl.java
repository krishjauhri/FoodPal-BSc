package client.scenes;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;

import com.google.inject.Inject;
import client.utils.ServerUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import java.util.Comparator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    @FXML
    private ListView<RecipeIngredient> ingredientsList;

    private ObservableList<Recipe> data;

    private Recipe selectedRecipe;

    @Inject
    public FoodPalMainCtrl(ServerUtils server) {
        this.server = server;
    }

    @FXML
    public void initialize() {
        // initialize list
        data = FXCollections.observableArrayList();
        colRecipeList.setItems(data);
        refreshRecipes();

        // listener for detail screen
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

                // add new recipe to the observable list
                data.add(saved);
            }
        });
    }
    @FXML
    private void addStep(){
        if(selectedRecipe == null){
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add step");
        dialog.setHeaderText("Add a new step");
        dialog.setContentText("Description:");

        dialog.showAndWait().ifPresent(text -> {
            if (!text.isBlank()) {
                int nextOrder = selectedRecipe.getSteps().size() + 1;
                server.addStep(selectedRecipe.getId(), nextOrder, text);
                refreshAndReloadSelected();
            }
        });
    }

    private void  editStep(Step step){
        TextInputDialog dialog = new TextInputDialog(step.getText());
        dialog.setTitle("Edit step");
        dialog.setHeaderText("Edit a step " + step.getOrder());
        dialog.setContentText("Description:");

        dialog.showAndWait().ifPresent(newText -> {
            if (!newText.isBlank() && !newText.equals(step.getText())) {
                server.updateStep(selectedRecipe.getId(), step.getId(), step.getOrder(), newText);
                refreshAndReloadSelected();
            }
        });
    }

    private void deleteStep(Step step){
        server.deleteStep(selectedRecipe.getId(), step.getId());
        refreshAndReloadSelected();
    }

    private void refreshAndReloadSelected(){
        long currentId = selectedRecipe.getId();
        refreshRecipes();

        Recipe updated = data.stream()
                .filter(r -> r.getId() == currentId)
                .findFirst()
                .orElse(null);
        if(updated != null){
            showRecipe(updated);
        }else{
            stepsBox.getChildren().clear();
            ingredientsList.getItems().clear();
            recipeTitle.setText("");
        }
    }
    public void showRecipe(Recipe recipe) {
        this.selectedRecipe = recipe;
        // title
        recipeTitle.setText(recipe.getName());
        showIngredients(recipe);

        // steps
        stepsBox.getChildren().clear();
        if (recipe.getSteps().isEmpty()) {
            stepsBox.getChildren().add(new Label("No steps found"));
        } else {
            //sort steps by order
            recipe.getSteps().sort(Comparator.comparingInt(Step::getOrder));

            for (Step step : recipe.getSteps()) {
                HBox row = new HBox(10);
                Label stepLabel = new Label(step.getOrder() + ". " +  step.getText());
                stepLabel.setWrapText(true);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                Button editBtn = new Button("Edit");
                editBtn.setOnAction(event -> editStep(step));

                Button deleteBtn = new Button("X");
                deleteBtn.setStyle("-fx-text-fill : red; -fx-font-weight: bold;");
                deleteBtn.setOnAction(event -> deleteStep(step));

                row.getChildren().addAll(stepLabel, spacer, editBtn, deleteBtn);
                stepsBox.getChildren().add(row);
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

    @FXML
    public void downloadRecipe() {
        if(selectedRecipe == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save recipe");
        fileChooser.setInitialFileName(recipeTitle.getText() + ".md");
        fileChooser.getExtensionFilters()
                .add(new  FileChooser.ExtensionFilter("Markdown Files", "*.md"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) {
            return;
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write("# " + selectedRecipe.getName() + "\n\n");
            bw.write("## Ingredients\n");
            if(selectedRecipe.getIngredients().isEmpty()) {
                bw.write("- No ingredients found\n");
            }
            else {
                for(RecipeIngredient ri : selectedRecipe.getIngredients()) {
                    bw.write("- " + ri.getAmount() + " " + ri.getUnit() + " " + ri.getIngredient().getName());
                    bw.write("\n");
                }
            }
            bw.write("\n");
            bw.write("## Steps\n");
            if(selectedRecipe.getSteps().isEmpty()) {
                bw.write("- No steps found");
            }
            else {
                for(Step step : selectedRecipe.getSteps()) {
                    bw.write("- " + step.getOrder() + ". " + step.getText() + "\n");
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }


    }

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

        refreshRecipes();// reload UI
        Recipe updated = data.stream()
                .filter(r -> r.getId() == recipe.getId())
                .findFirst()
                .orElse(recipe);

        showRecipe(updated);
    }

    @FXML
    private void editSelectedIngredient(){
        Recipe recipe = colRecipeList.getSelectionModel().getSelectedItem();
        RecipeIngredient selected = ingredientsList.getSelectionModel().getSelectedItem();

        if(recipe == null || selected == null) return;

        Double newAmount = askForNewAmount(selected);
        if (newAmount == null) return;

        String newUnit = askForNewUnit(selected);
        if (newUnit == null) return;

        updateIngredientOnServer(recipe, selected, newAmount, newUnit);

        refreshRecipes();// reload UI
        Recipe updated = data.stream()
                .filter(r -> r.getId() == recipe.getId())
                .findFirst()
                .orElse(recipe);

        showRecipe(updated);
    }
    private Double askForNewAmount(RecipeIngredient ri) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(ri.getAmount()));
        dialog.setTitle("Edit Ingredient");
        dialog.setHeaderText("Edit amount for " + ri.getIngredient().getName());
        dialog.setContentText("Amount:");

        var result = dialog.showAndWait();
        if (result.isEmpty()) return null;

        try {
            return Double.parseDouble(result.get());
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount");
            return null;
        }
    }

    private String askForNewUnit(RecipeIngredient ri) {
        TextInputDialog dialog = new TextInputDialog(ri.getUnit());
        dialog.setTitle("Edit Ingredient");
        dialog.setHeaderText("Edit unit for " + ri.getIngredient().getName());
        dialog.setContentText("Unit:");

        var result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void updateIngredientOnServer(
            Recipe recipe, RecipeIngredient ri, double amount, String unit) {

        server.updateIngredient(recipe.getId(), ri.getId(), amount, unit);
    }

    @FXML
    private void deleteSelectedIngredient() {
        Recipe recipe = colRecipeList.getSelectionModel().getSelectedItem();
        RecipeIngredient selected = ingredientsList.getSelectionModel().getSelectedItem();

        if (recipe == null || selected == null) return;

        server.deleteIngredient(recipe.getId(), selected.getId());

        refreshRecipes();
        Recipe updated = data.stream()
                .filter(r -> r.getId() == recipe.getId())
                .findFirst()
                .orElse(recipe);
        showRecipe(updated);
    }
    @FXML
    public void cloneRecipe(){
        if(selectedRecipe == null){
            return;
        }

        String newName = generateUniqueName(selectedRecipe.getName());
        Recipe clone = new Recipe(newName, new ArrayList<>(), new ArrayList<>());

        for(RecipeIngredient oldIngredient :selectedRecipe.getIngredients()){

            RecipeIngredient newIngredient = new RecipeIngredient(
                    clone,
                    oldIngredient.getIngredient(),
                    oldIngredient.getAmount(),
                    oldIngredient.getUnit());
            clone.addIngredient(newIngredient);
        }

        for(Step oldStep : selectedRecipe.getSteps()){
            Step newStep  = new Step(
                    clone,
                    oldStep.getOrder(),
                    oldStep.getText());
            clone.addStep(newStep);
        }

        try{
            Recipe savedClone = server.addRecipe(clone);
            refreshRecipes();
            colRecipeList.getSelectionModel().select(savedClone);
        }catch (Exception e){
            e.printStackTrace();

            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setGraphic(null);
            alert.setHeaderText("Something went wrong!\nCould not clone the recipe!");
            alert.setContentText("Make sure that the server is running!");
            alert.showAndWait();
        }
    }
    private String generateUniqueName(String originalName){
        int counter = 1;
        String candidateName = originalName + "(" + counter + ")";

        while(isNameTaken(candidateName)){
            counter ++;
            candidateName = originalName + "(" + counter + ")";
        }
        return candidateName;
    }

    private boolean isNameTaken(String name){
        return data.stream()
                .anyMatch(recipe -> recipe.getName().equals(name));
    }
}

