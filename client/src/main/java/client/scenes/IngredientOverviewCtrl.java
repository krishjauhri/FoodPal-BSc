package client.scenes;

import com.google.inject.Inject;
import commons.Ingredient;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.List;

public class IngredientOverviewCtrl {

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

    @Inject
    public IngredientOverviewCtrl() {

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

    private void showIngredient(Ingredient ingredient) {
        ingredientName.setText(ingredient.getName());
        proteinLabel.setText("Protein: " + ingredient.getProtein());
        fatLabel.setText("Fat: " + ingredient.getFat());
        carbsLabel.setText("Carbs: " + ingredient.getCarbs());
    }

    public void setIngredients(List<Ingredient> ingredients) {
        ingredientList.setItems(
                FXCollections.observableArrayList(ingredients)
        );
    }

    @FXML
    public void editIngredient() {
        Ingredient selected = ingredientList
                .getSelectionModel()
                .getSelectedItem();

        if (selected == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit nutrition");

        TextField proteinField = new TextField(String.valueOf(selected.getProtein()));
        TextField fatField = new TextField(String.valueOf(selected.getFat()));
        TextField carbsField = new TextField(String.valueOf(selected.getCarbs()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Protein:"), 0, 0);
        grid.add(proteinField, 1, 0);
        grid.add(new Label("Fat:"), 0, 1);
        grid.add(fatField, 1, 1);
        grid.add(new Label("Carbs:"), 0, 2);
        grid.add(carbsField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                selected.setProtein(Double.parseDouble(proteinField.getText()));
                selected.setFat(Double.parseDouble(fatField.getText()));
                selected.setCarbs(Double.parseDouble(carbsField.getText()));
                showIngredient(selected);
            }
        });
    }
}
