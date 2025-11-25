package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    private Ingredient i1, i2;

    @BeforeEach
    void setUp() {
        i1 = new Ingredient("butter", 100, "g");
        i2 = new Ingredient("butter", 100, "g");
    }

    @Test
    void testEquals() {
        assertTrue(i1.equals(i2));
    }

    @Test
    void testHashCode() {
        assertEquals(i1.hashCode(), i2.hashCode());
    }
}