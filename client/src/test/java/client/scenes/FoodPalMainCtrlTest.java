package client.scenes;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FoodPalMainCtrlTest {

    @Test
    void extractIngredientsReturnsSameIngredientInstance() {
        Ingredient flour = new Ingredient("Flour", 10, 1, 70);

        RecipeIngredient r1 = new RecipeIngredient(flour, 100, "g");
        RecipeIngredient r2 = new RecipeIngredient(flour, 200, "g");

        Recipe recipe1 = new Recipe("Pizza", List.of(r1), List.of());
        Recipe recipe2 = new Recipe("Bread", List.of(r2), List.of());

        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null);

        List<Ingredient> ingredients =
                ctrl.extractIngredients(List.of(recipe1, recipe2));

        assertEquals(1, ingredients.size());
        assertSame(flour, ingredients.get(0));
    }
}
