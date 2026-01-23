package client.scenes;

import commons.ShoppingItem;
import commons.Ingredient;
import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class ShoppingListCtrl {

    @FXML
    private ListView<ShoppingItem> shoppingListView;

    @FXML
    private ComboBox<Ingredient> ingredientBox;

    private final ObservableList<ShoppingItem> items = FXCollections.observableArrayList();

    private ServerUtils server;

    public void setServer(ServerUtils server){
        this.server = server;
        loadIngredients();
        refreshShoppingList();
    }

    private void loadIngredients() {
        try {
            List<Ingredient> serverIngredients = server.getIngredients();
            ingredientBox.setItems(FXCollections.observableArrayList(serverIngredients));
        } catch (Exception e) {
            showError("Connection Error", "Could not fetch ingredients.");
        }
    }

    public void refreshShoppingList() {
        try {
            List<ShoppingItem> serverItems = server.getShoppingList();
            items.setAll(serverItems);
        } catch (Exception e) {
            showError("Connection Error", "Could not fetch shopping list items.");
        }
    }

    @FXML
    public void initialize() {
        shoppingListView.setItems(items);

        shoppingListView.getSelectionModel().clearSelection();
        shoppingListView.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            if (items.isEmpty()) shoppingListView.getSelectionModel().clearSelection();
        });

        ingredientBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        ingredientBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        shoppingListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ShoppingItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }

                String name = (item.getIngredientName() == null) ? "" : item.getIngredientName().trim();
                String unit = (item.getUnit() == null) ? "" : item.getUnit().trim();
                double amount = item.getAmount();
                String source = (item.getSourceRecipe() == null) ? "" : item.getSourceRecipe().trim();

                if (amount > 0 && !unit.isBlank()) {
                    if(source.isEmpty()){
                        setText(amount + " " + unit + " " + name);
                    }
                    else{
                        setText(amount + " " + unit + " " + name + "(" + source + ")");
                    }
                } else {
                    setText(name);
                }
            }
        });
    }

    @FXML
    private void addItem() {
        if (server == null) {
            showError("Internal Error", "Server connection not initialized.");
            return;
        }
        Ingredient selectedIngredient = ingredientBox.getValue();
        if (selectedIngredient == null) {
            showError("No Selection", "Please select an ingredient first.");
            return;
        }

        Double amount = askForAmount();
        if (amount == null) return;

        String unit = askForUnit();
        if (unit == null) return;

        ShoppingItem newItem = new ShoppingItem(selectedIngredient.getName(), amount, unit);
        try {
            ShoppingItem savedItem = server.addShoppingItem(newItem);
            items.add(savedItem);
            ingredientBox.setValue(null);
        } catch (Exception e) {
            showError("Server Error", "Could not save shopping item to server.");
        }
    }

    private Ingredient askForIngredient() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Item");
        dialog.setHeaderText("Select an ingredient");

        try {
            List<Ingredient> serverIngredients = server.getIngredients();
            ingredientBox.setItems(FXCollections.observableArrayList(serverIngredients));
        } catch (Exception e) {
            showError("Connection Error", "Could not fetch ingredients.");
            return null;
        }

        // Display name in the dropdown
        ingredientBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) setText(null);
                else setText(item.getName());
            }
        });
        ingredientBox.setButtonCell(ingredientBox.getCellFactory().call(null));

        Button newIngredientBtn = new Button("Add new ingredient");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Ingredient:"), 0, 0);
        grid.add(ingredientBox, 1, 0);
        grid.add(newIngredientBtn, 2, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Logic to create new ingredient inside this dialog
        newIngredientBtn.setOnAction(event -> {
            Ingredient created = createNewIngredient();
            if (created != null) {
                ingredientBox.getItems().add(created);
                ingredientBox.getSelectionModel().select(created);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Ingredient selected = ingredientBox.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showError("No Selection", "Please select an ingredient.");
                return null;
            }
            return selected;
        }
        return null;
    }

    private Ingredient createNewIngredient() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Ingredient");
        dialog.setHeaderText("Create new ingredient");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()) return null;

        String name = result.get().trim();
        if (name.isEmpty()) {
            showError("Invalid name", "Ingredient name cannot be empty!");
            return null;
        }

        try {
            Ingredient newIngredient = new Ingredient(name, 0, 0, 0);
            return server.addIngredient(newIngredient);
        } catch (Exception e) {
            showError("Server error", "Can not create ingredient");
            return null;
        }
    }

    @FXML
    private void createNewIngredientMain() {
        Ingredient created = createNewIngredient();
        if (created != null) {
            ingredientBox.getItems().add(created);
            ingredientBox.setValue(created);
        }
    }

    private Double askForAmount() {
        while (true) {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setTitle("Amount");
            amountDialog.setHeaderText("Enter amount:");
            amountDialog.setContentText("Amount (Example: 200):");

            Optional<String> result = amountDialog.showAndWait();
            if (result.isEmpty()) return null; // User cancelled

            String raw = result.get().trim().replace(',', '.');
            try {
                double amount = Double.parseDouble(raw);
                if (!Double.isFinite(amount) || amount <= 0) {
                    showError("Invalid amount", "Amount must be a positive number.");
                    continue;
                }
                return amount;
            } catch (NumberFormatException e) {
                showError("Invalid amount", "Amount must be a number.");
            }
        }
    }

    private String askForUnit() {
        TextInputDialog unitDialog = new TextInputDialog();
        unitDialog.setTitle("Unit");
        unitDialog.setHeaderText("Enter unit:");
        unitDialog.setContentText("Unit (g, ml, pcs, ...):");

        while (true) {
            Optional<String> result = unitDialog.showAndWait();
            if (result.isEmpty()) return null; // User cancelled

            String unit = result.get().trim();
            if (!unit.isEmpty()) return unit; // Simple validation: just not empty

            showError("Invalid unit", "Unit cannot be empty.");
        }
    }

    // Alert messages
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @FXML
    private void removeSelected() {
        ShoppingItem selected = shoppingListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        try {
            server.deleteShoppingItem(selected.id);
            items.remove(selected);
        } catch (Exception e) {
            showError("Server Error", "Could not delete item from server.");
        }
    }

    @FXML
    private void resetList() {
        for(ShoppingItem sit : items){
            server.deleteShoppingItem(sit.getId());
        }
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

    public void addItems(List<ShoppingItem> newItems) {
        if (newItems == null || newItems.isEmpty()) return;
        items.addAll(newItems);
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