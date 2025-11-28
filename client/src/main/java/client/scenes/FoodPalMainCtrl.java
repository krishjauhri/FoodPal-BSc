package client.scenes;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import commons.Recipe;
import java.util.ArrayList;

public class FoodPalMainCtrl {

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
                colRecipeList.getItems().add(recipe);
            }
        });
    }

    public void initialize() {}
}
