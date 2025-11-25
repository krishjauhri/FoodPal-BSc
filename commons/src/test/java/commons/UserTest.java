package commons;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private Recipe recipe1;

    @BeforeEach
    void setUp() {
        user = new User(1, "Julia", new ArrayList<>());
        recipe1 = new Recipe("Pasta", new ArrayList<>(), new ArrayList<>());
    }

    @Test
    void addRecipe() {
        assertTrue(user.addRecipe(recipe1));
    }

    @Test
    void deleteRecipe() {
        user.addRecipe(recipe1);
        assertTrue(user.deleteRecipe(recipe1));
    }

    @Test
    void getRecipeByName() {
        user.addRecipe(recipe1);
        assertEquals(recipe1, user.getRecipeByName("Pasta"));
    }

    @Test
    void testEquals() {
        User other = user;
        assertTrue(user.equals(other));
    }

    @Test
    void testHashCode() {
        User other = user;
        assertTrue(user.hashCode() == other.hashCode());
    }
}