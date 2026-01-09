package client.scenes;

import commons.ShoppingItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ShoppingListCtrl {

    @FXML
    private ListView<ShoppingItem> shoppingListView;
    @FXML
    private TextField nameField;

    private final ObservableList<ShoppingItem> items = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        shoppingListView.setItems(items);

        shoppingListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ShoppingItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                String name = item.getIngredientName() == null ? "" : item.getIngredientName().trim();
                String unit = item.getUnit() == null ? "" : item.getUnit().trim();
                double amount = item.getAmount();

                if (amount > 0 && !unit.isBlank()) setText(amount + " " + unit + " " + name);
                else setText(name);
            }
        });
    }

    @FXML
    private void addItem() {
        String name = nameField.getText() == null ? "" : nameField.getText().trim();
        if (name.isEmpty()) return;

        items.add(new ShoppingItem(name));
        nameField.clear();
    }


    @FXML
    private void removeSelected() {
        ShoppingItem selected = shoppingListView.getSelectionModel().getSelectedItem();
        if (selected != null) items.remove(selected);
    }

    @FXML
    private void resetList() {
        items.clear();
    }

    @FXML
    private void downloadList() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save shopping list");
        fileChooser.setInitialFileName("shopping-list.md");
        fileChooser.getExtensionFilters()
                .add(new FileChooser.ExtensionFilter("Markdown Files", "*.md"));

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(toMarkdown());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void addItemForTest(ShoppingItem item) {
        items.add(item);
    }

    public String toMarkdown() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Shopping List\n\n");

        if (items.isEmpty()) {
            sb.append("- (empty)\n");
            return sb.toString();
        }

        for (ShoppingItem item : items) {
            String name = item.getIngredientName() == null ? "" : item.getIngredientName().trim();
            if (!name.isEmpty()) {
                sb.append("- ").append(name).append("\n");
            }
        }
        return sb.toString();
    }

}