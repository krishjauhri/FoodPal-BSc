package client.scenes;

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

public class ShoppingListOverviewCtrl {

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
        unitCol.setEditable(false); // keep unit non-editable for your scope
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