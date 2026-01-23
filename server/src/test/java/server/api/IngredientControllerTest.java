package server.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Ingredient;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import server.service.IngredientService;

import java.util.ArrayList;
import java.util.List;

public class IngredientControllerTest {

    private IngredientService ingredientService;
    private IngredientController sut;

    @BeforeEach
    void setup() {
        ingredientService = Mockito.mock(IngredientService.class);
        sut = new IngredientController(ingredientService);
    }

    @Test
    public void addIngredientTest() {
        Ingredient i = new Ingredient("tomato", 1, 2, 3);
        Ingredient saved = new Ingredient("tomato", 1, 2, 3);
        saved.setId(1L);

        when(ingredientService.addIngredient(i)).thenReturn(saved);

        Ingredient result = sut.addIngredient(i);

        assertEquals(saved, result);
        verify(ingredientService).addIngredient(i);
    }

    @Test
    public void getAllIngredientsTest() {
        List<Ingredient> list = new ArrayList<>();
        list.add(new Ingredient("tomato", 1, 2, 3));

        when(ingredientService.getAllIngredients()).thenReturn(list);

        List<Ingredient> result = sut.getAllIngredients();

        assertEquals(list, result);
        verify(ingredientService).getAllIngredients();
    }

    @Test
    public void deleteIngredientTest() {
        when(ingredientService.deleteIngredientEverywhere(1L)).thenReturn(true);

        ResponseEntity<Void> response = sut.deleteIngredient(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(ingredientService).deleteIngredientEverywhere(1L);
    }

    @Test
    void deleteIngredientNotFoundReturns404() {
        long id = 1L;
        when(ingredientService.deleteIngredientEverywhere(id)).thenReturn(false);

        ResponseEntity<Void> res = sut.deleteIngredient(id);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        verify(ingredientService).deleteIngredientEverywhere(id);
    }


    @Test
    public void deleteIngredientNotFoundTest() {
        when(ingredientService.deleteIngredientEverywhere(1L)).thenReturn(false);

        ResponseEntity<Void> response = sut.deleteIngredient(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(ingredientService).deleteIngredientEverywhere(1L);
    }


    @Test
    public void updateIngredientByIdTest() {
        Ingredient body = new Ingredient("tomato", 1, 2, 3);
        Ingredient updated = new Ingredient("tomato", 1, 2, 3);
        updated.setId(1L);

        when(ingredientService.updateIngredientById(1L, body)).thenReturn(updated);

        ResponseEntity<Ingredient> response = sut.updateIngredientById(1L, body);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updated, response.getBody());
        verify(ingredientService).updateIngredientById(1L, body);
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

