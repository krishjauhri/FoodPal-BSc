package client.scenes;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class IngredientTableTest {
    
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