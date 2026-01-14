package client.scenes;

import client.utils.WebSocketService;
import commons.*;
import com.google.inject.Inject;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import java.util.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.messaging.simp.stomp.StompSession;
import client.utils.ConfigService;
import javafx.scene.control.TextField;
import client.utils.RecipeSearchService;

public class FoodPalMainCtrl {


    private final ServerUtils server;
    private final ConfigService configService;
    private Parent ingredientView;
    private boolean ingredientLoaded = false;
    private IngredientOverviewCtrl ingredientCtrl;
    private Parent shoppingListView;
    private boolean shoppingListLoaded = false;
    private ShoppingListCtrl shoppingListCtrl;

    private final WebSocketService websocket;
    private StompSession.Subscription currentRecipeSubscription;



    private Node recipeView;

    @FXML
    private BorderPane contentPane;

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

    @FXML
    private TextField searchField;

    private final RecipeSearchService recipeSearchService = new RecipeSearchService();
    private List<Recipe> allRecipes = new ArrayList<Recipe>();


    ObservableList<Recipe> data;

    private Recipe selectedRecipe;
    private List<Recipe> recipes;

    @Inject
    public FoodPalMainCtrl(ServerUtils server, WebSocketService websocket, ConfigService configService) {
        this.server = server;
        this.configService = configService;
        this.websocket = websocket;
    }

    @FXML
    public void backRecipes() {
        contentPane.setCenter(recipeView);
    }
    @FXML
    public void showIngredientsList() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/client/scenes/IngredientOverview.fxml")
            );
            Parent view = loader.load();

            IngredientOverviewCtrl ctrl = loader.getController();

            List<Ingredient> ingredients = server.getIngredients();

            ctrl.setServer(server);
            ctrl.setMainCtrl(this);

            ctrl.setIngredients(ingredients);
            ctrl.setRecipes(server.getRecipes());


            contentPane.setCenter(view);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Initializes the controller after the FXML file has been loaded.
     * This method sets up the main UI components, including:
     *   Configuring the recipe list with a custom factory to handle the favorite star functionality.
     *   Establishing WebSocket subscriptions for real-time recipe updates from the server.
     *   Setting up selection listeners to update the detail view when a recipe is clicked.
     *   Performing the initial data fetch to populate the list.
     */
    @FXML
    public void initialize() {
        // initialize list
        data = FXCollections.observableArrayList();
        colRecipeList.setItems(data);
        colRecipeList.setCellFactory(r -> new RecipeListViewCell(configService));
        refreshRecipes();
        if(websocket.isConnected()) {
            websocket.subscribe("/topic/recipes", RecipeEvent.class, event -> {
                handleServerEvent(event);
            });
        }else{
            websocket.setConnectionListener(new WebSocketService.ConnectionListener() {
                @Override
                public void onConnectSuccess() {
                    websocket.subscribe("/topic/recipes", RecipeEvent.class, event -> {
                        handleServerEvent(event);
                    });
                }
                @Override
                public void onConnectFailed() { System.err.println("WebSocket Connection failed"); }
            });
        }
        // listener for detail screen
        colRecipeList.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldRecipe, newRecipe) -> {
                    if (newRecipe != null) {
                        showRecipe(newRecipe);
                    }
                });
        recipeView = contentPane.getCenter();
    }
    private void handleServerEvent(RecipeEvent event) {
        Platform.runLater(() -> {
            switch(event.type){
                case ADD:
                    Recipe newRecipe = new Recipe(event.name, new ArrayList<>(), new ArrayList<>());
                    newRecipe.setId(event.id);
                    data.add(newRecipe);
                    break;
                case DELETE:
                    if (configService.isFavourite(event.id)) {
                        configService.removeFavourite(event.id);

                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Favourite removed");
                        alert.setHeaderText("A favourite recipe was deleted");
                        alert.setContentText("A recipe you starred was deleted by someone else and was removed from your favourites.");
                        alert.showAndWait();
                    }
                    data.removeIf(r -> r.getId() == event.id);
                    //If somebody else has the currently deleted recipe opened, we clear the view
                    if(selectedRecipe != null && selectedRecipe.getId() == event.id){
                        selectedRecipe = null;
                        recipeTitle.setText("This recipe has been deleted, please select another one");
                        ingredientsList.getItems().clear();
                        stepsBox.getChildren().clear();
                    }
                    break;
                case UPDATE:
                    for (int i = 0; i < data.size(); i++) {
                        Recipe r = data.get(i);
                        if (r.getId() == event.id) {
                            r.setName(event.name);
                            data.set(i, r);
                            break;
                        }
                    }
                    if (selectedRecipe != null && selectedRecipe.getId() == event.id) {
                        recipeTitle.setText(event.name);
                    }
                    break;
            }
        });
    }
    @FXML
    public void refreshRecipes() {
        List<Recipe> recipesFromServer = server.getRecipes();
        allRecipes = new ArrayList<Recipe>(recipesFromServer);

        String q = "";
        if (searchField != null && searchField.getText() != null) {
            q = searchField.getText();
        }

        List<Recipe> filtered = recipeSearchService.filter(allRecipes, q);
        data.setAll(filtered);
        colRecipeList.getSelectionModel().clearSelection();
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
        refreshRecipes();
    }
    public void showRecipe(Recipe recipe) {
        this.selectedRecipe = recipe;

        // Remove old subscription
        if (currentRecipeSubscription != null) {
            currentRecipeSubscription.unsubscribe();
        }

        // Subscribe to new recipe channel
        if (websocket.isConnected()) {
            String topic = "/topic/recipes/" + recipe.getId();
            currentRecipeSubscription = websocket.subscribe(topic, Recipe.class, updatedRecipe -> {
                Platform.runLater(() -> {
                    if (selectedRecipe != null && selectedRecipe.getId() == updatedRecipe.getId()) {
                        renderRecipeDetails(updatedRecipe);
                    }
                });
            });
        }
        renderRecipeDetails(recipe);
    }

        private void renderRecipeDetails(Recipe recipe) {
        this.selectedRecipe = recipe;
        recipeTitle.setText(recipe.getName());
        showIngredients(recipe);

        stepsBox.getChildren().clear();
        if (recipe.getSteps().isEmpty()) {
            stepsBox.getChildren().add(new Label("No steps found"));
        } else {
            recipe.getSteps().sort(Comparator.comparingInt(Step::getOrder));
            for (Step step : recipe.getSteps()) {
                HBox row = new HBox(10);
                Label stepLabel = new Label(step.getOrder() + ". " + step.getText());
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
    private void editRecipeName() {
        if(selectedRecipe == null){
            return;
        }
        TextInputDialog dialog = new TextInputDialog(selectedRecipe.getName());
        dialog.setTitle("Edit recipe name");
        dialog.setHeaderText("Rename recipe");
        dialog.setContentText("New name:");

        dialog.showAndWait().ifPresent(newName -> {
            String trimmed = newName == null ? "" :  newName.trim();
            if(trimmed.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(null);
                alert.setHeaderText("Invalid recipe name");
                alert.setContentText("Recipe name cannot be empty");
                alert.showAndWait();
                return;
            }

            Recipe updated = new Recipe();
            updated.setName(trimmed);

            try {
                server.updateRecipe(selectedRecipe.getId(), updated);
                refreshAndReloadSelected();
            }
            catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(null);
                alert.setHeaderText("Could not update recipe name");
                alert.setContentText("Make sure the server is running");
                alert.showAndWait();
            }
        });
    }


    @FXML
    private void deleteSelectedRecipe() {
        if(selectedRecipe == null){
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setGraphic(null);
        confirm.setTitle("Delete recipe");
        confirm.setHeaderText("Are you sure you want to delete this recipe?");
        confirm.setContentText(selectedRecipe.getName());

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            server.deleteRecipe(selectedRecipe.getId());
            selectedRecipe = null;
            colRecipeList.getSelectionModel().clearSelection();
            refreshRecipes();
            recipeTitle.setText("Select a recipe");
            ingredientsList.getItems().clear();
            stepsBox.getChildren().clear();
        }
        catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setGraphic(null);
            alert.setHeaderText("Could not delete recipe");
            alert.setContentText("Make sure the server is running");
            alert.showAndWait();
        }
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

    public Ingredient createNewIngredient() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Ingredient");
        dialog.setHeaderText("Create new ingredient");
        dialog.setContentText("Name:");

        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty()){
            return null;
        }
        String name = result.get().trim();

        if (name.isEmpty()) {
            showError("Invalid name", "Ingredient name cannot be empty!");
            return null;
        }
        try {
            // nutrition values default to 0 for recipe interface
            Ingredient newIngredient = new Ingredient(name, 0, 0, 0);
            return server.addIngredient(newIngredient);
        } catch (Exception e) {
            showError("Server error", "Can not create ingredient");
            return null;
        }
    }

    @FXML
    private void addIngredient() {
        Recipe recipe = colRecipeList.getSelectionModel().getSelectedItem();
        if (recipe == null) return; // no recipe selected

        // choose an existing ingredient or create a new one
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Ingredient");

        // existing ingredients in a dropdown list
        ComboBox<Ingredient> ingredientBox = new ComboBox<>();
        List<Ingredient> ingredients = server.getIngredients();
        //change the normal list to list observable by javafx
        ObservableList<Ingredient> observableIngredients = FXCollections.observableArrayList(ingredients);
        ingredientBox.setItems(observableIngredients);

        // Button to create new one
        Button newIngredientBtn = new Button("Add new ingredient");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Ingredient:"), 0, 0);
        grid.add(ingredientBox, 1, 0);
        grid.add(newIngredientBtn, 2, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // creating new ingredient when clicking the Add new ingredient button
        newIngredientBtn.setOnAction(event -> {
            Ingredient created = createNewIngredient();
            //first add the new ingredient into the dropdown box, then select it automatically
            if (created != null) {
                ingredientBox.getItems().add(created);
                ingredientBox.getSelectionModel().select(created);
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        Ingredient ingredient = ingredientBox.getSelectionModel().getSelectedItem();
        if (ingredient == null) {
            showError("No ingredient selected", "Please select an ingredient first!");
            return;
        }


        Double amount;
        while (true) {
        TextInputDialog amountDialog = new TextInputDialog();
        amountDialog.setTitle("Amount");
        amountDialog.setHeaderText("Enter amount:");
        amountDialog.setContentText("Amount (Example: 200):");
        var amountResult = amountDialog.showAndWait();
        if (amountResult.isEmpty()) return;
        String raw =  amountResult.get().trim().replace(',', '.');
        try {
            amount = Double.parseDouble(raw);
            if(!Double.isFinite(amount) || amount <= 0) {
                showError("Invalid amount", "Amount must be a valid number.");
                continue;
            }
            break;
            }
        catch (NumberFormatException e) {
            showError("Invalid amount", "Amount must be a number or left empty.");
        }
        }

        String unit;
        TextInputDialog unitDialog = new TextInputDialog();
        unitDialog.setTitle("Unit");
        unitDialog.setHeaderText("Enter unit:");
        unitDialog.setContentText("Unit (g, ml, pcs, ...):");

        while (true) {
            var unitResult = unitDialog.showAndWait();
            if (unitResult.isEmpty()) return;

            unit = unitDialog.getEditor().getText().trim();
            if (isValidUnit(unit)) break;
            showError("Invalid unit", "Invalid unit, try again");
            unitDialog.getEditor().selectAll();
        }

        //sent to backend
        try {
            server.addIngredient(recipe.getId(), ingredient.getId(), amount, unit);
        }
        catch (Exception e) {
            showError("Server error", "Server rejected the value, try again");
        }

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

        try {
            updateIngredientOnServer(recipe, selected, newAmount, newUnit);
        }
        catch (Exception e) {
            showError("Server error", "Server rejected the value, try again");
            return;
        }

        refreshRecipes();// reload UI
        Recipe updated = data.stream()
                .filter(r -> r.getId() == recipe.getId())
                .findFirst()
                .orElse(recipe);

        showRecipe(updated);
    }
    private Double askForNewAmount(RecipeIngredient ri) {
        while(true) {
        TextInputDialog dialog = new TextInputDialog(String.valueOf(ri.getAmount()));
        dialog.setTitle("Edit Ingredient");
        dialog.setHeaderText("Edit amount for " + ri.getIngredient().getName());
        dialog.setContentText("Amount:");

        var result = dialog.showAndWait();
        if (result.isEmpty()) return null;
        String raw = result.get().trim().replace(',', '.');
        try {
            double v =  Double.parseDouble(raw);
            if(!Double.isFinite(v) || v <= 0) {
                showError("Invalid amount", "Amount must be a valid number, try again.");
                continue;
            }
            return v;
        }
        catch (NumberFormatException e) {
            showError("Invalid amount", "Amount must be a valid number, try again.");
        }
        }
    }

    private String askForNewUnit(RecipeIngredient ri) {
        while (true) {
            TextInputDialog dialog = new TextInputDialog(ri.getUnit());
            dialog.setTitle("Edit Ingredient");
            dialog.setHeaderText("Edit unit for " + ri.getIngredient().getName());
            dialog.setContentText("Unit:");

            var result = dialog.showAndWait();
            if (result.isEmpty()) return null;
            String u = result.get().trim();
            if (u.isEmpty()) {
                showError("Invalid unit", "Unit cannot be empty, try again");
                continue;
            }
            return u;
        }
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

    private void showError(String title, String message){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }


    private <T> T askUntilValid(String title, String header, String content, String initialValue, Function<String, T> parser) {
        String current = initialValue == null ? "" : initialValue;

        while(true) {
            TextInputDialog d = new TextInputDialog(current);
            d.setTitle(title);
            d.setHeaderText(header);
            d.setContentText(content);
            Optional<String> res = d.showAndWait();
            if (res.isPresent()) return null;

            String raw =  res.get().trim();
            try {
                T parsed = parser.apply(raw);
                current = raw;
                return parsed;
            } catch(IllegalArgumentException ex){
                showError(title, ex.getMessage() == null ? "Illegal value. Try again!" : ex.getMessage());
            } catch(Exception ex){
                showError(title, "Illegal value. Try again!");
            }
        }
    }

    private double askPositiveAmount(String initial) {
        Double v = askUntilValid(
                "Amount",
                "Enter a valid amount",
                "Amount (e.g., 100 or 12.5)",
                initial,
                s -> {
                    double x = Double.parseDouble(s.replace(',', '.'));
                    if(!Double.isFinite(x) || x <= 0) throw new IllegalArgumentException("Amount must be a valid number!");
                    return x;
                }
        );
        if (v == null) throw new RuntimeException("Cancelled");
        return v;
    }


    private String askNonEmptyUnit(String initial) {
        String u = askUntilValid(
                "Unit",
                "Enter a unit",
                "Unit (e.g., g, kg, ml, l)",
                initial,
                s -> {
                    String x =  s.trim();
                    if(x.isEmpty()) throw new RuntimeException("Unit cannot be empty!");
                    return x;
                }
        );
        if (u == null) throw new RuntimeException("Cancelled");
        return u;
    }


    private boolean isValidUnit(String unit) {
        if(unit == null) return false;
        String u =  unit.trim();
        return !u.isEmpty() && u.matches(".*[A-Za-z].*");
    }

    @FXML
    public void showShoppingList() {
        try {
            if (!shoppingListLoaded) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/client/scenes/ShoppingList.fxml")
                );
                shoppingListView = loader.load();
                shoppingListCtrl = loader.getController();
                shoppingListLoaded = true;
            }
            contentPane.setCenter(shoppingListView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

