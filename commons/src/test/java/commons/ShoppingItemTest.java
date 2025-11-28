package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ShoppingItemTest {

    @Test
    public void testConstructor() {
        var s = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        assertEquals("Salt", s.getIngredientName());
        assertEquals(5, s.getAmount());
        assertEquals("g", s.getUnit());
        assertEquals("Pizza Dough", s.getSourceRecipe());
    }

    @Test
    public void testGettersAndSetters() {
        var item = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        item.setIngredientName("Sugar");
        item.setAmount(10);
        item.setUnit("h");
        item.setSourceRecipe("Cake");

        assertEquals("Sugar", item.getIngredientName());
        assertEquals(10, item.getAmount());
        assertEquals("h", item.getUnit());
        assertEquals("Cake", item.getSourceRecipe());
    }

    @Test
    public void testEqualsSameValues() {
        var a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var b = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        assertEquals(a, b);
    }

    @Test
    public void testEqualsDifferentValues() {
        var a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var b = new Shoppingitem("Pepper", 7, "ml", "Pasta");
        assertNotEquals(a, b);
    }

    @Test
    public void testToStringContainsFields() {
        var item = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var str = item.toString();
        assertTrue(str.contains("Salt"));
        assertTrue(str.contains("5.0"));
        assertTrue(str.contains("g"));
        assertTrue(str.contains("Pizza Dough"));
    }

    @Test
    public void testHashCodeSameValues() {
        var a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var b = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    public void testHashCodeDifferentValues() {
        var a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var b = new Shoppingitem("Pepper", 7, "ml", "Pasta");
        assertNotEquals(a.hashCode(), b.hashCode());
    }
}
