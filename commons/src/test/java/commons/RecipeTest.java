package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    private Recipe r1, r2;
    private Ingredient flour, water, coffee, sugar;
    private Step s1, s2, s3, s4;

    @BeforeEach
    void setUp(){
        flour = new Ingredient("Flour", 200, "g");
        water = new Ingredient("Water", 100, "ml");
        sugar = new Ingredient("Sugar", 20, "g");
        coffee = new Ingredient("Coffee", 8, "tablespoons");

        s1 = new Step(1, "Mix ingredients");
        s2 = new Step(2, "Let the dough rest");
        s3 = new Step(2, "Add the coffee");
        s4 = new Step(3, "Add the sugar");

        List<Ingredient> ing1 = new ArrayList<>();
        ing1.add(flour);
        ing1.add(water);

        List<Ingredient> ing2 = new ArrayList<>();
        ing2.add(water);
        ing2.add(coffee);
        ing2.add(sugar);

        List<Step> steps1 = new ArrayList<>();
        steps1.add(s1);
        steps1.add(s2);

        List<Step> steps2 = new ArrayList<>();
        steps2.add(s1);
        steps2.add(s3);
        steps2.add(s4);

        r1 = new Recipe("Dough", ing1, steps1);
        r2 = new Recipe("Latte", ing2, steps2);

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
        Ingredient salt = new Ingredient("Salt", 5, "g");
        r1.addIngredient(salt);
        assertTrue(r1.getIngredients().contains(salt));
    }

    @Test
    void removeIngredient() {
        Ingredient salt = new Ingredient("Salt", 5, "g");
        r1.addIngredient(salt);
        r1.removeIngredient(salt);
        assertFalse(r1.getIngredients().contains(salt));
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