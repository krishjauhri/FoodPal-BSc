package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RecipeIngredientTest {

    private Ingredient ingredient;
    private RecipeIngredient r1, r2, r3;

    @BeforeEach
    public void setUp() {
        ingredient = new Ingredient("Flour", 10, 1, 75);
        r1 = new RecipeIngredient(ingredient, 500, "g");
        r2 = new RecipeIngredient(ingredient, 500, "g");
        r3 = new RecipeIngredient(ingredient, 250, "kg");
    }

    @Test
    public void testConstructor() {
        assertNotNull(r1.getIngredient());
        assertEquals(ingredient, r1.getIngredient());
        assertEquals(500, r1.getAmount());
        assertEquals("g", r1.getUnit());
    }

    @Test
    public void testConstructorThrowExceptionForAmountEqualToZero(){
        assertThrows(IllegalArgumentException.class, () -> new RecipeIngredient(ingredient, 0, "g"));
    }

    @Test
    public void testConstructorThrowExceptionForAmountNegative(){
        assertThrows(IllegalArgumentException.class, () -> new RecipeIngredient(ingredient, -50, "g"));
    }

    @Test
    public void testSetters(){
        var newIngredient = new Ingredient("Flour", 10, 1, 75);

        r1.setIngredient(newIngredient);
        r1.setAmount(100);
        r1.setUnit("spoons");

        assertEquals(newIngredient, r1.getIngredient());
        assertEquals(100, r1.getAmount());
        assertEquals("spoons", r1.getUnit());
    }

    @Test
    public void setAmountThrowsExceptionWhenAmountIsNotValid(){
        assertThrows(IllegalArgumentException.class, () ->{
            r1.setAmount(0);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            r1.setAmount(-20);
        });
    }
}
