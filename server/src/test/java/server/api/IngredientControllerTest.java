package server.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Ingredient;
import org.springframework.http.ResponseEntity;
import server.service.IngredientService;
import java.util.List;

public class IngredientControllerTest {

    private IngredientController sut;
    private FakeIngredientService service;

    @BeforeEach
    public void setup() {
        service = new FakeIngredientService();
        sut = new IngredientController(service);
    }

    @Test
    public void addIngredientTest() {
        Ingredient i = new Ingredient("tomato", 1, 2, 3);
        i.setId(1L);
        Ingredient result = sut.addIngredient(i);
        assertTrue(service.addCalled);
        assertEquals(i, result);
    }

    @Test
    public void getAllIngredientsTest() {
        sut.getAllIngredients();
        assertTrue(service.getAllCalled);
    }

    @Test
    public void deleteIngredientTest() {
        ResponseEntity<Void> response = sut.deleteIngredients(1L);
        assertTrue(service.deleteCalled);
        assertEquals(1L, service.deleteID);

    }

    public static class FakeIngredientService extends IngredientService {

        boolean addCalled = false;
        boolean getAllCalled = false;
        boolean deleteCalled = false;
        Long deleteID = null;

        FakeIngredientService() {
            super(null, null);
        }

        @Override
        public Ingredient addIngredient(Ingredient ingredient) {
            addCalled = true;
            return ingredient;
        }

        @Override
        public List<Ingredient> getAllIngredients() {
            getAllCalled = true;
            return List.of();
        }

        @Override
        public void deleteIngredientInRecipe(Long id) {
            deleteCalled = true;
            deleteID = id;
        }
    }
}

