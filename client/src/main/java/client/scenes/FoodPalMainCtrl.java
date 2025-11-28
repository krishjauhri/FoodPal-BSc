package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

public class FoodPalMainCtrl {

    @FXML
    private ListView<String> colRecipeList;

    @FXML
    private void addRecipe() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add recipe");
        dialog.setHeaderText(null);
        dialog.setContentText("Recipe name:");

        dialog.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                colRecipeList.getItems().add(name);
            }
        });
    }

    public void initialize() {}
}
