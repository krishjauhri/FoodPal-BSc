package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    private Ingredient i1, i2, i3;

    @BeforeEach
    void setUp() {
        i1 = new Ingredient("Flour", 2, 3,2);
        i2 = new Ingredient("Flour", 2, 3,2);
        i3 = new Ingredient("", 1, 1,0);
    }

    @Test
    void testEquals() {
        assertTrue(i1.equals(i2));
    }

    @Test
    void testHashCode() {
        assertEquals(i1.hashCode(), i2.hashCode());
    }

    @Test
    void calculateKcalPer100gCorrectly() {
        Ingredient ingredient = new Ingredient("Test", 10, 5, 20);
        double kcal = ingredient.calculateKcalPer100g();
        assertEquals(165.0, kcal);
    }
}