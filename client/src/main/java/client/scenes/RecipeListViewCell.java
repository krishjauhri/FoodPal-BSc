package client.scenes;

import client.utils.ConfigService;
import commons.Recipe;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Custom ListCell implementation for displaying a Recipe in a ListView.
 * This cell includes the recipe name and a toggleable favorite star button.
 */
public class RecipeListViewCell extends ListCell<Recipe> {

    private final ConfigService configService;
    private final HBox hbox = new HBox();
    private final Label nameLabel = new Label();
    private final Button favouriteStar = new Button();
    private final Region spacer = new Region();

    /**
     * Initializes a new instance of the cell.
     * Configures the layout alignment, spacers, and button click handlers.
     * @param configService configService The service used to manage local user configurations and favorites.
     */
    public RecipeListViewCell(ConfigService configService) {
        this.configService = configService;

        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(nameLabel, spacer, favouriteStar);

        favouriteStar.setOnAction(e -> {
            Recipe item = getItem();
            if(item != null) {
                configService.toggleFavorite(item.getId());
                updateIcon(item);
            }
        });
    }

    /**
     * Called by JavaFX to update the cell content.
     * Handles rendering the name and star if data is present, or clearing the cell if empty.
     *
     * @param item  The Recipe object to display.
     * @param empty Boolean indicating if the cell is currently empty.
     */
    @Override
    protected void updateItem(Recipe item, boolean empty) {
        super.updateItem(item, empty);

        if(empty || item == null) {
            setText(null);
            setGraphic(null);
        }else{
            nameLabel.setText(item.getName());
            updateIcon(item);

            setText(null);
            setGraphic(hbox);
        }
    }

    /**
     * Synchronizes the star button's visual appearance with the favorite status
     * stored in the local config service.
     *
     * @param item The recipe whose favorite status is to be checked.
     */
    private void updateIcon(Recipe item) {
        boolean isFavourite = configService.isFavourite(item.getId());
        favouriteStar.setText("★");

        if(isFavourite) {
            favouriteStar.setStyle("-fx-background-color: transparent; -fx-text-fill: gold; -fx-font-size: 15");
        }else{
            favouriteStar.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-font-size: 15 ");
        }
    }

}
