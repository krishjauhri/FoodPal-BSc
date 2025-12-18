package commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class RecipeEventTest {

    @Test
    public void testConstructorAndGetters() {
        RecipeEvent event = new RecipeEvent(RecipeEvent.Type.ADD, 123L, "Pizza");

        assertEquals(RecipeEvent.Type.ADD, event.type);
        assertEquals(123L, event.id);
        assertEquals("Pizza", event.name);
    }

    @Test
    public void testEmptyConstructor() {
        // Required for JSON mapping
        RecipeEvent event = new RecipeEvent();
        assertNotNull(event);
    }
}