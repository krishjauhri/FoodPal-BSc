package server.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import commons.Ingredient;
import server.database.IngredientRepository;
import server.database.RecipeRepository;

public class IngredientServiceTest {

    private IngredientService sut;
    private IngredientRepository ingredientRepo;
    private RecipeRepository recipeRepo;

    @BeforeEach
    public void setup() {
        //mock repo
        ingredientRepo = mock(IngredientRepository.class);
        recipeRepo = mock(RecipeRepository.class);

        sut = new IngredientService(ingredientRepo, recipeRepo);
    }

    @Test
    public void addIngredient() {
        Ingredient ingredient = new Ingredient("tomato", 1, 2, 3);
        when(ingredientRepo.save(ingredient)).thenReturn(ingredient);
        Ingredient result = sut.addIngredient(ingredient);

        verify(ingredientRepo).save(ingredient);
        assertEquals(ingredient, result);
    }

    @Test
    public void getAllIngredients() {
        when(ingredientRepo.findAll()).thenReturn(List.of());
        sut.getAllIngredients();

        verify(ingredientRepo).findAll();
    }

    @Test
    public void deleteIngredient() {
        Ingredient ingredient = new Ingredient("salt", 0, 0, 0);
        ingredient.setId(1L);
        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ingredient);
        List<RecipeIngredient> ingredients = new ArrayList<>();
        ingredients.add(ri);

        Recipe recipe = new Recipe("soup", ingredients, new ArrayList<>());

        when(ingredientRepo.findById(1L)).thenReturn(Optional.of(ingredient));
        when(recipeRepo.findAll()).thenReturn(List.of(recipe));

        sut.deleteIngredientInRecipe(1L);

        verify(ingredientRepo).findById(1L);
        verify(recipeRepo).findAll();
        verify(recipeRepo).save(recipe);
        verify(ingredientRepo).delete(ingredient);
        assertTrue(recipe.getIngredients().isEmpty());
    }


}
