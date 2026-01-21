package server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import commons.Recipe;
import commons.RecipeEvent;
import server.service.IngredientService;
import server.service.RecipeService;

public class RecipeControllerTest {

    private RecipeService recipeService;
    private IngredientService ingredientService;
    private SimpMessagingTemplate msgs;

    private RecipeController sut;


    @BeforeEach
    public void setup() {
        recipeService = Mockito.mock(RecipeService.class);
        ingredientService = Mockito.mock(IngredientService.class);
        msgs = Mockito.mock(SimpMessagingTemplate.class);

        sut = new RecipeController(recipeService, ingredientService, msgs);
    }


    @Test
    public void addRecipeBroadcastsEvent() {
        Recipe recipe = new Recipe("Pizza", new ArrayList<>(), new ArrayList<>());
        Recipe savedRecipe = new Recipe("Pizza", new ArrayList<>(), new ArrayList<>());
        savedRecipe.setId(1L);

        when(recipeService.addRecipe(recipe)).thenReturn(savedRecipe);

        sut.add(recipe);

        verify(recipeService).addRecipe(recipe);
        verify(msgs).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }


    @Test
    public void deleteRecipeBroadcastsEvent() {
        long id = 5L;
        when(recipeService.deleteRecipe(id)).thenReturn(true);

        ResponseEntity<Void> response = sut.deleteRecipe(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(recipeService).deleteRecipe(id);
        verify(msgs).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }

    @Test
    public void deleteRecipeNotFoundDoesNotBroadcast() {
        long id = 5L;
        when(recipeService.deleteRecipe(id)).thenReturn(false);

        ResponseEntity<Void> response = sut.deleteRecipe(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(recipeService).deleteRecipe(id);
        verify(msgs, never()).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }

    @Test
    public void updateRecipeBroadcastsEvent() {
        long id = 1L;
        Recipe saved = new Recipe("New Name", new ArrayList<>(), new ArrayList<>());
        saved.setId(id);

        Recipe updateInfo = new Recipe("New Name", null, null);

        when(recipeService.updateRecipeName(id, "New Name")).thenReturn(saved);

        ResponseEntity<Recipe> response = sut.updateRecipe(id, updateInfo);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(recipeService).updateRecipeName(id, "New Name");
        verify(msgs).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }

    @Test
    public void addIngredientBroadcastsUpdate() {
        long recipeId = 1L;
        RecipeIngredient ri = new RecipeIngredient();
        Recipe updated = new Recipe("Pizza", new ArrayList<>(), new ArrayList<>());
        updated.setId(recipeId);

        when(recipeService.addIngredient(recipeId, ri)).thenReturn(updated);

        sut.addIngredientToRecipe(recipeId, ri);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }

    @Test
    public void addStepBroadcastsUpdate() {
        long recipeId = 1L;
        commons.Step step = new commons.Step();
        Recipe updated = new Recipe("Pizza", null, null);
        updated.setId(recipeId);

        when(recipeService.addStep(recipeId, step)).thenReturn(updated);

        sut.addStepToRecipe(recipeId, step);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }

    @Test
    public void updateIngredientBroadcastsUpdate() {
        long recipeId = 1L;
        long riId = 2L;
        RecipeController.UpdateIngredientRequest req = new RecipeController.UpdateIngredientRequest(500, "g");

        Recipe updated = new Recipe();
        updated.setId(recipeId);

        when(recipeService.updateIngredient(recipeId, riId, 500, "g")).thenReturn(updated);

        sut.updateIngredient(recipeId, riId, req);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }

    @Test
    public void updateStepBroadcastsUpdate() {
        long recipeId = 1L;
        long stepId = 2L;
        RecipeController.UpdateStepRequest req = new RecipeController.UpdateStepRequest(5, "Bake it");

        Recipe updated = new Recipe();
        updated.setId(recipeId);

        when(recipeService.updateStep(recipeId, stepId, 5, "Bake it")).thenReturn(updated);

        sut.updateStep(recipeId, stepId, req);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }

    @Test
    public void deleteIngredientBroadcastsUpdate() {
        long recipeId = 1L;
        long riId = 2L;
        Recipe updated = new Recipe();
        updated.setId(recipeId);

        when(recipeService.deleteIngredient(recipeId, riId)).thenReturn(updated);

        sut.deleteIngredient(recipeId, riId);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }

    @Test
    public void deleteStepBroadcastsUpdate() {
        long recipeId = 1L;
        long stepId = 2L;
        Recipe updated = new Recipe();
        updated.setId(recipeId);

        when(recipeService.deleteStep(recipeId, stepId)).thenReturn(updated);

        sut.deleteStep(recipeId, stepId);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }
}