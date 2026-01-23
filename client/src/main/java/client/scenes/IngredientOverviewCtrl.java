package client.scenes;

import client.utils.ServerUtils;
import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.util.List;
import java.util.Optional;

public class IngredientOverviewCtrl {

    private ServerUtils server;
    private FoodPalMainCtrl mainCtrl;
    private List<Recipe> recipes;

    @FXML
    private ListView<Ingredient> ingredientList;

    @FXML
    private Label ingredientName;

    @FXML
    private Label proteinLabel;

    @FXML
    private Label fatLabel;

    @FXML
    private Label carbsLabel;

    @FXML
    private Label kcalLabel;

    @FXML
    private Label usageLabel;

    public void setServer(ServerUtils server) {
        this.server = server;
    }

    public void setMainCtrl(FoodPalMainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    @FXML
    public void initialize() {
        ingredientList.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldIng, newIng) -> {
                    if (newIng != null) {
                        showIngredient(newIng);
                    }
                });
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }


    public int countUsage(Ingredient ingredient) {
    if (recipes == null || ingredient == null) {
        return 0;
    }
    int count = 0;
    for (Recipe recipe : recipes) {
        for (RecipeIngredient ri : recipe.getIngredients()) {
            Ingredient riIngredient = ri.getIngredient();
            if (riIngredient == null) continue;

            boolean same;

            if (ingredient.getId() != null && riIngredient.getId() != null) {
                same = riIngredient.getId().equals(ingredient.getId());
            }
            else {
                same = riIngredient == ingredient;
            }
            if (same) {
                count++;
                break;
            }
        }
    }
    return count;
}

    private void showIngredient(Ingredient ingredient) {
        ingredientName.setText(ingredient.getName());
        proteinLabel.setText("Protein: " + ingredient.getProtein());
        fatLabel.setText("Fat: " + ingredient.getFat());
        carbsLabel.setText("Carbs: " + ingredient.getCarbs());

        double kcal = ingredient.calculateKcalPer100g();
        kcalLabel.setText("Kcal / 100g: " + String.format("%.1f", kcal));

        int usage = countUsage(ingredient);
        usageLabel.setText("Used in " + usage + " recipe(s)");
    }

    public void setIngredients(List<Ingredient> ingredients) {
        ingredientList.setItems(FXCollections.observableArrayList(ingredients));
    }

    @FXML
    public void deleteIngredient(){
        Ingredient ingredient = ingredientList.getSelectionModel().getSelectedItem();
        int usage = countUsage(ingredient);
        if (usage > 0) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete ingredient");
            alert.setHeaderText(" Ingredient is used");
            alert.setContentText("This ingredient is used in " + usage + " recipe(s).\n"
                    + "If you continue, it will be removed from all the recipes");

            Optional<ButtonType> result = alert.showAndWait();
            //get rid of the warning interface / choose cancel button
            if(result.isEmpty() ||  result.get() != ButtonType.OK){
                return;
            }
        }
        server.deleteIngredient(ingredient.getId());
        ingredientList.getItems().remove(ingredient);
        mainCtrl.refreshRecipes();

    }



    @FXML
    public void editIngredient() {
        Ingredient selected = ingredientList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit nutrition");

        TextField nameField = new TextField(selected.getName());
        TextField proteinField = new TextField(String.valueOf(selected.getProtein()));
        TextField fatField = new TextField(String.valueOf(selected.getFat()));
        TextField carbsField = new TextField(String.valueOf(selected.getCarbs()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Protein:"), 0, 1);
        grid.add(proteinField, 1, 1);
        grid.add(new Label("Fat:"), 0, 2);
        grid.add(fatField, 1, 2);
        grid.add(new Label("Carbs:"), 0, 3);
        grid.add(carbsField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                selected.setName(nameField.getText());
                selected.setProtein(Double.parseDouble(proteinField.getText()));
                selected.setFat(Double.parseDouble(fatField.getText()));
                selected.setCarbs(Double.parseDouble(carbsField.getText()));

                server.updateIngredient(selected);

                if (mainCtrl != null) {
                    mainCtrl.refreshRecipes();
                }

                showIngredient(selected);
            }
        });
    }

    @FXML
    public void addIngredient() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add ingredient");

        TextField nameField = new TextField();
        TextField proteinField = new TextField();
        TextField fatField = new TextField();
        TextField carbsField = new TextField();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Protein:"), 0, 1);
        grid.add(proteinField, 1, 1);
        grid.add(new Label("Fat:"), 0, 2);
        grid.add(fatField, 1, 2);
        grid.add(new Label("Carbs:"), 0, 3);
        grid.add(carbsField, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                Ingredient ingredient = new Ingredient(
                        nameField.getText(),
                        Double.parseDouble(proteinField.getText()),
                        Double.parseDouble(fatField.getText()),
                        Double.parseDouble(carbsField.getText())
                );

                Ingredient saved = server.addIngredient(ingredient);
                ingredientList.getItems().add(saved);
            }
        });
    }
}
