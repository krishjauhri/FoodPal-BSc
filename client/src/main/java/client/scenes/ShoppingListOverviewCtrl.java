package client.scenes;

import client.utils.ServerUtils;
import commons.Ingredient;
import commons.ShoppingItem;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;

import java.util.List;
import java.util.Optional;

public class ShoppingListOverviewCtrl {

    @FXML
    private ComboBox<Ingredient> ingredientBox;

    @FXML
    private TableView<ShoppingItem> table;

    @FXML
    private TableColumn<ShoppingItem, String> nameCol;

    @FXML
    private TableColumn<ShoppingItem, Double> amountCol;

    @FXML
    private TableColumn<ShoppingItem, String> unitCol;

    private final ObservableList<ShoppingItem> rows = FXCollections.observableArrayList();

    private FoodPalMainCtrl mainCtrl;

    private ServerUtils server;

    public void setServer(ServerUtils server) {
        this.server = server;
        loadIngredients();
    }

    private void loadIngredients() {
        try {
            List<Ingredient> serverIngredients = server.getIngredients();
            ingredientBox.setItems(FXCollections.observableArrayList(serverIngredients));
        } catch (Exception e) {
            showError("Connection Error", "Could not fetch ingredients.");
        }
    }

    @FXML
    public void initialize() {
        table.setItems(rows);
        table.setEditable(true);

        nameCol.setCellValueFactory(cd ->
                new SimpleStringProperty(safe(cd.getValue().getIngredientName()))
        );
        nameCol.setEditable(false);

        amountCol.setCellValueFactory(cd ->
                new SimpleObjectProperty<>(cd.getValue().getAmount())
        );
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amountCol.setEditable(true);

        amountCol.setOnEditCommit(evt -> {
            Double vObj = evt.getNewValue();
            if (vObj == null || !Double.isFinite(vObj) || vObj <= 0) {
                showError("Invalid amount", "Amount must be a number > 0");
                table.refresh();
                return;
            }
            evt.getRowValue().setAmount(vObj);
        });

        unitCol.setCellValueFactory(cd ->
                new SimpleStringProperty(safe(cd.getValue().getUnit()))
        );
        unitCol.setEditable(false);

        ingredientBox.setCellFactory(cb -> new ListCell<>() {
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
    }

    public void setMainCtrl(FoodPalMainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    public void setItems(List<ShoppingItem> items) {
        rows.setAll(items);
    }

    @FXML
    private void addToShoppingList() {
        if (mainCtrl == null) return;
        mainCtrl.addOverviewItemsToShoppingList(java.util.List.copyOf(rows));
    }

    @FXML
    private void backToRecipes() {
        if (mainCtrl != null) {
            mainCtrl.backRecipes();
        }
    }

    @FXML
    private void addRow() {
        Ingredient selected = ingredientBox.getValue();
        if (selected == null) {
            showError("No Selection", "Please select an ingredient first.");
            return;
        }

        Double amount = askForAmount();
        if (amount == null) return;

        String unit = askForUnit();
        if (unit == null) return;

        ShoppingItem newItem = new ShoppingItem(selected.getName(), amount, unit);
        rows.add(newItem);
        ingredientBox.setValue(null);
    }

    private Double askForAmount() {
        while (true) {
            TextInputDialog amountDialog = new TextInputDialog();
            amountDialog.setTitle("Amount");
            amountDialog.setHeaderText("Enter amount:");
            amountDialog.setContentText("Amount (Example: 200):");

            Optional<String> result = amountDialog.showAndWait();
            if (result.isEmpty()) return null;

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
            if (result.isEmpty()) return null;

            String unit = result.get().trim();
            if (!unit.isEmpty()) return unit;
            showError("Invalid unit", "Unit cannot be empty.");
        }
    }

    @FXML
    private void deleteRow() {
        ShoppingItem selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showError("No Selection", "Please select a row to delete.");
            return;
        }
        rows.remove(selected);
    }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}