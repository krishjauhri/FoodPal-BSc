package commons;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShowRecipeTest {

    @Test
    void testRecipeName() {
        Recipe r = new Recipe();
        r.setName("Tomato Soup");
        assertEquals("Tomato Soup", r.getName());
    }

    @Test
    void testRecipeNameChanges() {
        Recipe r = new Recipe();
        r.setName("Old Name");
        r.setName("New Name");
        assertEquals("New Name", r.getName());
    }

    @Test
    void testRecipeIngredientsStored() {
        Recipe r = new Recipe();
        r.setName("Tomato Soup");
        Ingredient ing1 = new Ingredient();
        ing1.setName("Potato");
        Ingredient ing2 = new Ingredient();
        ing2.setName("Tomato");
        RecipeIngredient ri1 = new RecipeIngredient(ing1, 300.0, "g");
        RecipeIngredient ri2 = new RecipeIngredient(ing2, 300.0, "g");
        r.addIngredient(ri1);
        r.addIngredient(ri2);
        assertEquals(2, r.getIngredients().size());
        assertEquals("Potato", r.getIngredients().get(0).getIngredient().getName());
        assertEquals("Tomato", r.getIngredients().get(1).getIngredient().getName());
        assertEquals(300, r.getIngredients().get(0).getAmount());
        assertEquals("g", r.getIngredients().get(0).getUnit());
        assertEquals(300, r.getIngredients().get(1).getAmount());
        assertEquals("g", r.getIngredients().get(1).getUnit());
    }

    @Test
    void testRecipeStepsInOrder() {
        Recipe r = new Recipe();
        r.setName("Cake");
        Step s1 = new Step(1, "Prepare ingredients");
        Step s2 = new Step(2, "Mix ingredients");
        Step s3 = new Step(2, "Bake in oven");
        Step s4 = new Step(3, "Let it cool");
        Step s5 = new Step(4, "Add cream");
        r.addStep(s1);
        r.addStep(s2);
        r.addStep(s3);
        r.addStep(s4);
        r.addStep(s5);
        assertEquals(5, r.getSteps().size());
        assertEquals("Prepare ingredients", r.getSteps().get(0).getText());
        assertEquals("Mix ingredients", r.getSteps().get(1).getText());
        assertEquals("Bake in oven", r.getSteps().get(2).getText());
        assertEquals("Let it cool", r.getSteps().get(3).getText());
        assertEquals("Add cream", r.getSteps().get(4).getText());
    }

    @Test
    void testEmptyIngredients() {
        Recipe r = new Recipe();
        r.setName("Tomato Soup");
        assertTrue(r.getIngredients().isEmpty());
        assertEquals(0, r.getIngredients().size());
    }

    @Test
    void testEmptySteps() {
        Recipe r = new Recipe();
        r.setName("Tomato Soup");
        assertTrue(r.getSteps().isEmpty());
        assertEquals(0, r.getSteps().size());
    }

    @Test
    void testRecipeDataMatchesExpectations() {
        Recipe r = new Recipe();
        r.setName("Tomato Pasta");
        Ingredient ing = new Ingredient();
        ing.setName("Noodle");
        RecipeIngredient ri = new RecipeIngredient(ing, 300.0, "g");
        Step s = new Step(1, "Boil noodles");
        r.addIngredient(ri);
        r.addStep(s);
        assertEquals("Tomato Pasta", r.getName());
        assertEquals(1, r.getIngredients().size());
        assertEquals("Noodle", r.getIngredients().get(0).getIngredient().getName());
        assertEquals(300, r.getIngredients().get(0).getAmount());
        assertEquals("g", r.getIngredients().get(0).getUnit());
        assertEquals("Boil noodles", r.getSteps().get(0).getText());
    }
}
