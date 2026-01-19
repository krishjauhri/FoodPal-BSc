package client.scenes;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecipeKalTest {

    @Test
    public void emptyIngredientRecipe() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null, null, null);
        Recipe recipe = new Recipe("Empty", List.of(), List.of());
        double kcal = ctrl.calculateRecipeKal(recipe);
        assertEquals(0.0, kcal);
    }
    @Test
    public void singleIngredientRecipe() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null, null, null);
        Ingredient sugar = new Ingredient("sugar", 0, 0, 100);
        RecipeIngredient ri = new RecipeIngredient(sugar, 100, "g");
        Recipe recipe = new Recipe("cake", List.of(ri), List.of());
        double kcal = ctrl.calculateRecipeKal(recipe);
        // kcal/100g = 100 * 4 = 400
        assertEquals(400, kcal);
    }

    @Test
    public void multipleIngredientsRecipe() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null, null, null);
        Ingredient chicken = new Ingredient("chicken", 20, 5, 0);
        Ingredient rice = new Ingredient("rice", 7, 1, 77);
        RecipeIngredient ri1 = new RecipeIngredient(chicken, 200, "g");
        RecipeIngredient ri2 = new RecipeIngredient(rice, 100, "g");
        Recipe recipe = new Recipe("chicken Rice", List.of(ri1, ri2), List.of());
        double kcal = ctrl.calculateRecipeKal(recipe);
        assertEquals(198.33, kcal,0.01);
    }

    @Test
    public void kgConvertedToG() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null, null, null);
        Ingredient flour = new Ingredient("flour", 10, 1, 76);
        RecipeIngredient ri = new RecipeIngredient(flour, 1, "kg");
        Recipe recipe = new Recipe("bread", List.of(ri), List.of());
        double kcal = ctrl.calculateRecipeKal(recipe);
        // kcal/100g = 10*4 + 1*9 + 76*4 = 353
        assertEquals(353, kcal);
    }

    @Test
    public void skipMLUnit() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null, null, null);
        Ingredient milk = new Ingredient("milk", 10, 1, 76);
        RecipeIngredient ri = new RecipeIngredient(milk, 100, "ml");
        Recipe recipe = new Recipe("bread", List.of(ri), List.of());
        double kcal = ctrl.calculateRecipeKal(recipe);
        assertEquals(0, kcal);

    }

}
