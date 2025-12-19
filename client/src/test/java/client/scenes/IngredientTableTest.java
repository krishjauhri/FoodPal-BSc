package client.scenes;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class IngredientTableTest {

    @Test
    void extractIngredients() {
        Ingredient egg = new Ingredient("Egg", 13, 11, 1);
        Ingredient milk = new Ingredient("Milk", 3.4, 3.6, 5);

        RecipeIngredient r1i1 = new RecipeIngredient(egg, 1, "pcs");
        RecipeIngredient r1i2 = new RecipeIngredient(milk, 100, "ml");
        RecipeIngredient r2i1 = new RecipeIngredient(egg, 2, "pcs"); // duplicate
        Recipe r1 = new Recipe("R1",
                List.of(r1i1, r1i2),
                List.of());
        Recipe r2 = new Recipe("R2",
                List.of(r2i1),
                List.of());
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null);
        List<Ingredient> result =
                ctrl.extractIngredients(List.of(r1, r2));

        assertEquals(2, result.size());
        assertTrue(result.contains(egg));
        assertTrue(result.contains(milk));
    }

    @Test
    void extractIngredientsFromEmptyRecipes() {
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null);
        List<Ingredient> result = ctrl.extractIngredients(List.of());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void extractIngredientsFromRecipeWithoutIngredients() {
        Recipe recipe = new Recipe(
                "Empty recipe",
                List.of(),
                List.of()
        );
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null);
        List<Ingredient> result =
                ctrl.extractIngredients(List.of(recipe));
        assertTrue(result.isEmpty());
    }

    @Test
    void extractIngredientsIgnoresNullIngredients() {
        RecipeIngredient valid =
                new RecipeIngredient(new Ingredient("Milk", 3, 3, 5), 100, "ml");
        RecipeIngredient invalid =
                new RecipeIngredient(null, 100, "ml");
        Recipe recipe = new Recipe(
                "Test",
                List.of(valid, invalid),
                List.of()
        );
        FoodPalMainCtrl ctrl = new FoodPalMainCtrl(null);
        List<Ingredient> result =
                ctrl.extractIngredients(List.of(recipe));
        assertEquals(1, result.size());
        assertEquals("Milk", result.get(0).getName());
    }

    @Test
    void ingredientUsageCountsRecipeOnlyOnce() {
        Ingredient flour = new Ingredient("Flour", 0, 0, 0);

        RecipeIngredient r1 = new RecipeIngredient(flour, 100, "g");
        RecipeIngredient r2 = new RecipeIngredient(flour, 200, "g");

        Recipe recipe = new Recipe("Bread", List.of(r1, r2), List.of());

        IngredientOverviewCtrl ctrl = new IngredientOverviewCtrl();
        ctrl.setRecipes(List.of(recipe));

        int usage = ctrl.countUsage(flour);

        assertEquals(1, usage);
    }

    @Test
    void ingredientUsageIsZeroWhenNotUsed() {
        Ingredient flour = new Ingredient("Flour", 0, 0, 0);

        Recipe recipe = new Recipe("Soup", List.of(), List.of());

        IngredientOverviewCtrl ctrl = new IngredientOverviewCtrl();
        ctrl.setRecipes(List.of(recipe));

        int usage = ctrl.countUsage(flour);

        assertEquals(0, usage);
    }


}
