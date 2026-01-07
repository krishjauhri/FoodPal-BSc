package server.api;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Ingredient;
import server.service.IngredientService;

public class IngredientControllerTest {

    private IngredientController sut;
    //fake service
    private FakeIngredientService service;

    @BeforeEach
    public void setup() {
        service = new FakeIngredientService();
        sut = new IngredientController(service);
    }

    @Test
    public void addIngredientTest() {
        Ingredient i = new Ingredient("tomato", 1, 2, 3);
        Ingredient result = sut.addIngredient(i);
        assertTrue(service.addCalled);
    }

    @Test
    public void getAllIngredientsTest() {
        sut.getAllIngredients();
        assertTrue(service.getAllCalled);
    }

    //fake service
    public static class FakeIngredientService extends IngredientService {

        boolean addCalled = false;
        boolean getAllCalled = false;

        FakeIngredientService() {
            super(null);
        }

        @Override
        public Ingredient addIngredient(Ingredient ingredient) {
            addCalled = true;
            return ingredient;
        }

        @Override
        public java.util.List<Ingredient> getAllIngredients() {
            getAllCalled = true;
            return java.util.List.of();
        }
    }
}
