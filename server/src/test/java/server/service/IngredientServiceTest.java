package server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Ingredient;
import server.database.IngredientRepository;

public class IngredientServiceTest {

    private IngredientService sut;
    private IngredientRepository repo;

    @BeforeEach
    public void setup() {
        //mock repo
        repo = mock(IngredientRepository.class);
        sut = new IngredientService(repo);
    }

    @Test
    public void addIngredientTest() {
        Ingredient i = new Ingredient("milk", 1, 2, 3);
        //mock will return null, make it return the ingredient instead
        when(repo.save(i)).thenReturn(i);
        Ingredient result = sut.addIngredient(i);
        //verify the repo save method was called
        verify(repo).save(i);
        assertEquals(i, result);
    }

    @Test
    public void getAllIngredientsTest() {
        when(repo.findAll()).thenReturn(List.of());
        sut.getAllIngredients();
        verify(repo).findAll();
    }
}
