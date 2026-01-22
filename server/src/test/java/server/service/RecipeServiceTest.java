package server.service;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import server.database.IngredientRepository;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;
import server.database.StepRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock RecipeRepository recipeRepository;
    @Mock RecipeIngredientRepository recipeIngredientRepository;
    @Mock StepRepository stepRepository;
    @Mock IngredientRepository ingredientRepository;

    @InjectMocks RecipeService sut;

    private Recipe recipe;
    private Ingredient ingredient;

    @BeforeEach
    void setup() {
        recipe = new Recipe();
        recipe.setId(1L);
        recipe.setIngredients(new ArrayList<>());
        recipe.setSteps(new ArrayList<>());

        ingredient = new Ingredient();
        ingredient.setId(10L);
        ingredient.setName("Salt");
    }

    @Test
    void addIngredient_success_addsAndSaves() {
        RecipeIngredient incoming = new RecipeIngredient();
        Ingredient ref = new Ingredient();
        ref.setId(10L);
        incoming.setIngredient(ref);
        incoming.setAmount(5.0);
        incoming.setUnit("g");

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(ingredient));
        when(recipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = sut.addIngredient(1L, incoming);

        assertEquals(1, updated.getIngredients().size());
        assertEquals(5, updated.getIngredients().get(0).getAmount());
        assertEquals("g", updated.getIngredients().get(0).getUnit());

        verify(recipeIngredientRepository).save(any(RecipeIngredient.class));
        verify(recipeRepository).save(recipe);
    }

    @Test
    void addIngredient_missingAmountOrUnit_throws() {
        RecipeIngredient incoming = new RecipeIngredient();
        Ingredient ref = new Ingredient();
        ref.setId(10L);
        incoming.setIngredient(ref);
        // missing amount/unit

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(ingredientRepository.findById(10L)).thenReturn(Optional.of(ingredient));

        assertThrows(IllegalArgumentException.class,
                () -> sut.addIngredient(1L, incoming));

        verify(recipeRepository, never()).save(any());
        verifyNoInteractions(recipeIngredientRepository);
    }

    @Test
    void updateIngredient_success_updatesAndSaves() {
        RecipeIngredient existing = spy(new RecipeIngredient(recipe, ingredient, 2.0, "g"));
        doReturn(99L).when(existing).getId();
        recipe.getIngredients().add(existing);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = sut.updateIngredient(1L, 99L, 10.0, "kg");

        assertEquals(10, updated.getIngredients().get(0).getAmount());
        assertEquals("kg", updated.getIngredients().get(0).getUnit());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void deleteIngredient_success_removesAndSaves() {
        RecipeIngredient existing = spy(new RecipeIngredient(recipe, ingredient, 2.0, "g"));
        doReturn(77L).when(existing).getId();
        recipe.getIngredients().add(existing);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = sut.deleteIngredient(1L, 77L);

        assertTrue(updated.getIngredients().isEmpty());
        verify(recipeRepository).save(recipe);
    }

    @Test
    void updateServings_success_setsAndSaves() {
        when(recipeRepository.findById(1L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Recipe updated = sut.updateServings(1L, 4);

        assertEquals(4, updated.getServings());
        verify(recipeRepository).save(recipe);
    }
}
