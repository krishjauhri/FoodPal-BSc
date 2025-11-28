package commons;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ShoppingItemTest {
    @Test
    public void testConstructor() {
        Shoppingitem s = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        assertEquals("Salt", s.getIngredientName());
        assertEquals(5, s.getAmount());
        assertEquals("g", s.getUnit());
        assertEquals("Pizza Dough", s.getSourceRecipe());
    }
    @Test
    public void testGettersAndSetters() {
        Shoppingitem m = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        m.setIngredientName("Sugar");
        m.setAmount(10);
        m.setUnit("h");
        m.setSourceRecipe("Cake");
        assertEquals("Sugar", m.getIngredientName());
        assertEquals(10, m.getAmount());
        assertEquals("h", m.getUnit());
        assertEquals("Cake", m.getSourceRecipe());
    }
    @Test
    public void testEquals() {
        Shoppingitem t = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        Shoppingitem r = new Shoppingitem("Pepper", 7, "ml", "Pasta");
        Shoppingitem s = new Shoppingitem("Pepper", 7, "ml", "Pasta");
        assertEquals(t, r);
        assertEquals(t, s);
        assertNotEquals(r, s);
        assertNotEquals(r, t);
    }
    @Test
    public void testToString() {
        var t = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        var real = t.toString();
        assertEquals("Salt", real);
        assertTrue(real.contains("Salt"));
    }

    @Test
    public void testHashCode() {
        Shoppingitem a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        Shoppingitem b = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        assertEquals(a.hashCode(), b.hashCode());
    }
    @Test
    public void testNotHashCode() {
        Shoppingitem a = new Shoppingitem("Salt", 5, "g", "Pizza Dough");
        Shoppingitem r = new Shoppingitem("Pepper", 7, "ml", "Pasta");
        assertNotEquals(a.hashCode(), r.hashCode());
    }
}
