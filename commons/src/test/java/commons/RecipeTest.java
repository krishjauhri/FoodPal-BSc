package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    private Recipe r1, r2;
    private Ingredient flour, water, coffee, sugar;
    private RecipeIngredient flourRecipe, waterRecipe, coffeeRecipe, sugarRecipe;
    private Step s1, s2, s3, s4;

    @BeforeEach
    void setUp(){
        flour = new Ingredient("Flour", 2, 1, 4);
        water = new Ingredient("Water", 1, 0, 0);
        sugar = new Ingredient("Sugar", 3, 2, 5);
        coffee = new Ingredient("Coffee", 8, 4, 1);

        flourRecipe = new RecipeIngredient(flour, 100, "g");
        waterRecipe = new RecipeIngredient(water, 50, "ml");
        sugarRecipe = new RecipeIngredient(sugar, 25, "g");
        coffeeRecipe = new RecipeIngredient(coffee, 100, "tablespoon");

        s1 = new Step(1, "Mix ingredients");
        s2 = new Step(2, "Let the dough rest");
        s3 = new Step(2, "Add the coffee");
        s4 = new Step(3, "Add the sugar");

        List<RecipeIngredient> ing1 = new ArrayList<>();
        ing1.add(flourRecipe);
        ing1.add(waterRecipe);

        List<RecipeIngredient> ing2 = new ArrayList<>();
        ing2.add(waterRecipe);
        ing2.add(coffeeRecipe);
        ing2.add(sugarRecipe);

        List<Step> steps1 = new ArrayList<>();
        steps1.add(s1);
        steps1.add(s2);

        List<Step> steps2 = new ArrayList<>();
        steps2.add(s1);
        steps2.add(s3);
        steps2.add(s4);

        r1 = new Recipe("Dough", ing1, steps1);
        r2 = new Recipe("Latte", ing2, steps2);
        r1.setId(1L);
        r2.setId(2L);
    }
    @Test
    void testEquals() {
        assertNotEquals(r1, r2);
    }

    @Test
    void testHashCode() {
        assertNotEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    void addIngredient() {
        Ingredient salt = new Ingredient("Salt", 5, 3, 1);
        RecipeIngredient saltRecipe = new RecipeIngredient(salt, 100, "g");
        r1.addIngredient(saltRecipe);
        assertTrue(r1.getIngredients().contains(saltRecipe));
    }

    @Test
    void removeIngredient() {
        Ingredient salt = new Ingredient("Salt", 5, 3, 1);
        RecipeIngredient saltRecipe = new RecipeIngredient(salt, 100, "g");
        r1.addIngredient(saltRecipe);
        r1.removeIngredient(saltRecipe);
        assertFalse(r1.getIngredients().contains(saltRecipe));
    }

    @Test
    void addStep() {
        Step s5 = new Step(3, "Bake it");
        r1.addStep(s5);
        assertTrue(r1.getSteps().contains(s5));
    }

    @Test
    void removeStep() {
        Step s5 = new Step(3, "Bake it");
        r1.addStep(s5);
        r1.removeStep(s5);
        assertFalse(r1.getSteps().contains(s5));
    }
}