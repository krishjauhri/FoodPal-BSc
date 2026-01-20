package client.scenes;

import client.MyFXML;
import client.utils.WebSocketService;
import commons.*;
import com.google.inject.Inject;
import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

import javafx.util.Pair;
import org.springframework.messaging.simp.stomp.StompSession;
import client.utils.ConfigService;
import javafx.scene.control.TextField;
import client.utils.RecipeSearchService;

public class FoodPalMainCtrl {


    private final ServerUtils server;
    private final ConfigService configService;
    private Parent shoppingListView;
    private boolean shoppingListLoaded = false;
    private ShoppingListCtrl shoppingListCtrl;

    private MyFXML myFXML;

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

    @FXML
    private ToggleButton allRecipesButton;

    @FXML
    private ToggleButton favouriteRecipesButton;

    @FXML
    private ToggleGroup filterGroup;

    private final RecipeSearchService recipeSearchService = new RecipeSearchService();
    private List<Recipe> allRecipes = new ArrayList<Recipe>();


    ObservableList<Recipe> data;

    private Recipe selectedRecipe;
    private double scaleFactor = 1.0; // client-only
    private List<Recipe> recipes;


    private Parent shoppingOverviewView;
    private boolean shoppingOverviewLoaded = false;
    private ShoppingListOverviewCtrl shoppingOverviewCtrl;


    @Inject
    public FoodPalMainCtrl(ServerUtils server, WebSocketService websocket, ConfigService configService) {
        this.server = server;
        this.configService = configService;
        this.websocket = websocket;
    }

    public void setMyFXML(MyFXML myFXML) {
        this.myFXML = myFXML;
    }

    @FXML
    public void backRecipes() {
        contentPane.setCenter(recipeView);
    }
    @FXML
    public void showIngredientsList() {
        try {
            if (myFXML == null) {
                throw new IllegalStateException("MyFXML not set on FoodPalMainCtrl");
            }

            Pair<IngredientOverviewCtrl, Parent> pair =
                    myFXML.load(IngredientOverviewCtrl.class,
                            "client", "scenes", "IngredientOverview.fxml");

            IngredientOverviewCtrl ctrl = pair.getKey();
            Parent view = pair.getValue();

            ctrl.setServer(server);
            ctrl.setMainCtrl(this);

            ctrl.setIngredients(server.getIngredients());
            ctrl.setRecipes(server.getRecipes());


            contentPane.setCenter(view);

        } catch (Exception e) {
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

        if (searchField != null) {
            searchField.setOnAction(event -> refreshRecipes());
        }

        if (filterGroup != null) {
            filterGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null) {
                    oldVal.setSelected(true);
                } else {
                    refreshRecipes();
                }
            });
        }

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
//                    Recipe newRecipe = new Recipe(event.name, new ArrayList<>(), new ArrayList<>());
//                    newRecipe.setId(event.id);
//                    data.add(newRecipe);
                    refreshRecipes();
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

                    refreshRecipes();
//                    data.removeIf(r -> r.getId() == event.id);
                    //If somebody else has the currently deleted recipe opened, we clear the view
                    if(selectedRecipe != null && selectedRecipe.getId() == event.id){
                        selectedRecipe = null;
                        recipeTitle.setText("This recipe has been deleted, please select another one");
                        ingredientsList.getItems().clear();
                        stepsBox.getChildren().clear();
                    }
                    break;
                case UPDATE:
//                    for (int i = 0; i < data.size(); i++) {
//                        Recipe r = data.get(i);
//                        if (r.getId() == event.id) {
//                            r.setName(event.name);
//                            data.set(i, r);
//                            break;
//                        }
//                    }
                    refreshRecipes();
                    if (selectedRecipe != null && selectedRecipe.getId() == event.id) {
                        recipeTitle.setText(event.name);
                    }
                    break;
            }
        });
    }
    @FXML
    private void editServings() {
        if (selectedRecipe == null) {
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit servings");

        TextField servingsField = new TextField(String.valueOf(inferServingsIfNeeded(selectedRecipe)));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Servings:"), 0, 0);
        grid.add(servingsField, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    int newServings = Integer.parseInt(servingsField.getText().trim());
                    if (newServings <= 0) {
                        showError("Invalid servings", "Servings must be > 0");
                        return;
                    }
                    selectedRecipe.setServings(newServings);
                    server.updateRecipeServings(selectedRecipe.getId(), newServings);
                    renderRecipeDetails(selectedRecipe);
                } catch (NumberFormatException e) {
                    showError("Invalid servings", "Servings must be a whole number");
                }
            }
        });
    }

    public double calculateRecipeKal(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return 0;
        }
        double totalKcal = 0;
        double totalWeight = 0;
        for (RecipeIngredient ri : recipe.getIngredients()) {
            Ingredient ingredient = ri.getIngredient();

            if (ingredient == null) {
                continue;
            }
            //skip ingredients with units other than g/kg
            String unit = ri.getUnit();
            if (!unit.equals("g") && !unit.equals("kg")) {
                continue;
            }
            String unitCheck = ri.getUnit();
            if (!isMassUnit(unitCheck)) continue;
            double amountInGrams = toGrams(ri.getAmount(), unitCheck);
            double kcalPer100g = ingredient.calculateKcalPer100g();
            double ingredientKcal = kcalPer100g * (amountInGrams / 100.0);
            totalKcal += ingredientKcal;
            totalWeight += amountInGrams;
        }
        if (totalWeight == 0) {
            return 0;
        }
        return totalKcal / totalWeight * 100;
    }

    private int inferServingsIfNeeded(Recipe recipe) {
        if (recipe == null) return 1;

        int s = recipe.getServings();
        if (s > 0) return s;

        double grams = 0;
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri == null || ri.getIngredient() == null) continue;
            if (!isMassUnit(ri.getUnit())) continue;
            grams += toGrams(ri.getAmount(), ri.getUnit());
        }

        int inferred = (int) Math.round(grams / 250.0);
        return Math.max(1, inferred);
    }



    @FXML
    public void refreshRecipes() {
        List<Recipe> recipesFromServer = server.getRecipes();
        allRecipes = new ArrayList<Recipe>(recipesFromServer);
        List <Recipe> viewFiltered;

        if(favouriteRecipesButton != null && favouriteRecipesButton.isSelected()){
            viewFiltered = allRecipes.stream()
                    .filter(r -> configService.isFavourite(r.getId()))
                    .toList();
        }else{
            viewFiltered = allRecipes;
        }

        String q = "";

        if (searchField != null && searchField.getText() != null) {
            q = searchField.getText();
        }

        List<Recipe> filtered = recipeSearchService.filter(viewFiltered, q);

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
            double kcalPer100g = calculateRecipeKal(recipe);
            NutritionScaled totals = calculateTotalKcalScaled(recipe, scaleFactor);

            int baseServings = inferServingsIfNeeded(recipe);
            double scaledServings = baseServings * scaleFactor;

            recipeTitle.setText(
                    recipe.getName()
                            + "\n" + String.format("%.0f kcal / 100g", kcalPer100g)
                            + " | Total: " + String.format("%.0f kcal", totals.kcal())
                            + " | P: " + String.format("%.1fg", totals.protein())
                            + " F: " + String.format("%.1fg", totals.fat())
                            + " C: " + String.format("%.1fg", totals.carbs())
                            + " | Servings: " + String.format("%.1f", scaledServings)
                            + " | Scale: x" + String.format("%.2f", scaleFactor)

            );

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
                        .toList()
        );

        ingredientsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(RecipeIngredient item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.displayAmountScaled(scaleFactor));
            }
        });
    }

    private static boolean isMassUnit(String u) {
        if (u == null) return false;
        String x = u.trim().toLowerCase();
        return x.equals("g") || x.equals("kg");
    }

    private static double toGrams(double amount, String unit) {
        String u = unit.trim().toLowerCase();
        return u.equals("kg") ? amount * 1000.0 : amount;
    }

    private record NutritionScaled(double kcal, double protein, double fat, double carbs, double grams) {}

    private NutritionScaled calculateTotalKcalScaled(Recipe recipe, double factor) {
        if (recipe == null || recipe.getIngredients() == null) {
            return new NutritionScaled(0, 0, 0, 0, 0);
        }
        if (!Double.isFinite(factor) || factor <= 0) factor = 1.0;

        double gramsTotal = 0.0;
        double proteinTotal = 0.0;
        double fatTotal = 0.0;
        double carbsTotal = 0.0;

        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri == null || ri.getIngredient() == null) continue;
            if (!isMassUnit(ri.getUnit())) continue;

            double grams = toGrams(ri.getAmount(), ri.getUnit()) * factor;
            gramsTotal += grams;

            Ingredient ing = ri.getIngredient();

            // assumes macros are per 100g
            proteinTotal += ing.getProtein() * (grams / 100.0);
            fatTotal     += ing.getFat()     * (grams / 100.0);
            carbsTotal   += ing.getCarbs()   * (grams / 100.0);
        }

        double kcalTotal = 4.0 * proteinTotal + 9.0 * fatTotal + 4.0 * carbsTotal;
        return new NutritionScaled(kcalTotal, proteinTotal, fatTotal, carbsTotal, gramsTotal);
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
    private void changeScaleFactor() {
        if (selectedRecipe == null) return;

        TextInputDialog d = new TextInputDialog(String.valueOf(scaleFactor));
        d.setTitle("Scale recipe");
        d.setHeaderText("Enter scale factor (e.g., 0.5, 2, 3.5)");
        d.setContentText("Scale factor:");

        Optional<String> res = d.showAndWait();
        if (res.isEmpty()) return;

        String raw = res.get().trim().replace(',', '.');
        try {
            double f = Double.parseDouble(raw);
            if (!Double.isFinite(f) || f <= 0) {
                showError("Invalid factor", "Factor must be > 0");
                return;
            }
            scaleFactor = f;
            renderRecipeDetails(selectedRecipe);
        } catch (NumberFormatException e) {
            showError("Invalid factor", "Factor must be a number");
        }
    }



    @FXML
    public void downloadRecipe() {
        if(selectedRecipe == null) {
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save recipe");
        fileChooser.setInitialFileName(selectedRecipe.getName() + ".md");
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
                    bw.write("- " + ri.displayAmountScaled(scaleFactor));
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

            Double amount = askForFormalAmount();
            if (amount == null) return;

            String unit = askForFormalUnit();
            if (unit == null) return;

            try {
                server.addIngredient(recipe.getId(), ingredient.getId(), amount, unit);
            } catch (Exception e) {
                showError("Server error", "Server rejected the value, try again");
                return;
            }

            refreshRecipes();
            Recipe updated = data.stream()
                    .filter(r -> r.getId() == recipe.getId())
                    .findFirst()
                    .orElse(recipe);
            showRecipe(updated);

    }

    private Double askForFormalAmount() {
        TextInputDialog d = new TextInputDialog();
        d.setTitle("Amount");
        d.setHeaderText("Enter amount (number)");
        d.setContentText("Amount:");
        Optional<String> res = d.showAndWait();
        if (res.isEmpty()) return null;

        String raw = res.get().trim().replace(',', '.');
        try {
            double v = Double.parseDouble(raw);
            if (!Double.isFinite(v) || v <= 0) {
                showError("Invalid amount", "Amount must be > 0");
                return null;
            }
            return v;
        } catch (NumberFormatException e) {
            showError("Invalid amount", "Amount must be a number");
            return null;
        }
    }

    private String askForFormalUnit() {
        TextInputDialog d = new TextInputDialog("g");
        d.setTitle("Unit");
        d.setHeaderText("Enter unit (e.g., g, kg)");
        d.setContentText("Unit:");
        Optional<String> res = d.showAndWait();
        if (res.isEmpty()) return null;

        String u = res.get().trim();
        if (u.isEmpty()) {
            showError("Invalid unit", "Unit cannot be empty");
            return null;
        }
        return u;
    }

    @FXML
    private void resetScale() {
        scaleFactor = 1.0;
        if (selectedRecipe != null) {
            renderRecipeDetails(selectedRecipe);
        }
    }


    @FXML
    private void editSelectedIngredient() {
        Recipe recipe = colRecipeList.getSelectionModel().getSelectedItem();
        RecipeIngredient selected = ingredientsList.getSelectionModel().getSelectedItem();

        if (recipe == null || selected == null) return;

        Double newAmount = askForNewAmount(selected);
        if (newAmount == null) return;

        String newUnit = askForNewUnit(selected);
        if (newUnit == null) return;

        try {
            server.updateIngredient(recipe.getId(), selected.getId(), newAmount, newUnit);
        } catch (Exception e) {
            showError("Server error", "Server rejected the value, try again");
            return;
        }

        refreshRecipes();
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
            if (res.isEmpty()) return null;

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
                if (myFXML == null) {
                    throw new IllegalStateException("MyFXML not set on FoodPalMainCtrl");
                }
                Pair<ShoppingListCtrl, Parent> pair =
                        myFXML.load(ShoppingListCtrl.class,
                                "client", "scenes", "ShoppingList.fxml");
                shoppingListCtrl = pair.getKey();
                shoppingListView = pair.getValue();
                shoppingListLoaded = true;
                if(shoppingListCtrl != null) {
                    shoppingListCtrl.setServer(server);
                }
            }
            contentPane.setCenter(shoppingListView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void openShoppingOverviewForSelectedRecipe() {
        if (selectedRecipe == null) return;
        try {
            if (!shoppingOverviewLoaded) {
                if (myFXML == null) {
                    throw new IllegalStateException("MyFXML not set on FoodPalMainCtrl");
                }
                Pair<ShoppingListOverviewCtrl, Parent> pair =
                        myFXML.load(ShoppingListOverviewCtrl.class,
                                "client", "scenes", "ShoppingListOverview.fxml");
                shoppingOverviewCtrl = pair.getKey();
                shoppingOverviewView = pair.getValue();
                shoppingOverviewCtrl.setMainCtrl(this);
                shoppingOverviewLoaded = true;
            }
            List<ShoppingItem> rows = selectedRecipe.getIngredients().stream()
                    .filter(ri -> ri.getIngredient() != null)
                    .map(ri -> new ShoppingItem(
                            ri.getIngredient().getName(),
                            ri.getAmount() * scaleFactor,
                            ri.getUnit(),
                            selectedRecipe.getName()
                    ))
                    .toList();
            shoppingOverviewCtrl.setItems(rows);
            contentPane.setCenter(shoppingOverviewView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addOverviewItemsToShoppingList(List<ShoppingItem> overviewItems) {
        if (overviewItems == null || overviewItems.isEmpty()) return;
        if (!shoppingListLoaded) {
            try {
                if (myFXML == null) {
                    throw new IllegalStateException("MyFXML not set on FoodPalMainCtrl");
                }
                Pair<ShoppingListCtrl, Parent> pair =
                        myFXML.load(ShoppingListCtrl.class,
                                "client", "scenes", "ShoppingList.fxml");
                shoppingListCtrl = pair.getKey();
                shoppingListView = pair.getValue();
                shoppingListLoaded = true;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        shoppingListCtrl.addItems(overviewItems);
        contentPane.setCenter(shoppingListView);
    }
}

