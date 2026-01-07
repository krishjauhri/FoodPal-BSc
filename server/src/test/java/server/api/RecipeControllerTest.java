package server.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import commons.Recipe;
import commons.RecipeEvent;
import server.database.IngredientRepository;
import server.database.RecipeRepository;
import server.database.StepRepository;
import server.service.RecipeService;

public class RecipeControllerTest {

    private RecipeRepository repo;
    private RecipeService recipeService;
    private IngredientRepository ingredientRepo;
    private StepRepository stepRepo;
    private SimpMessagingTemplate msgs;

    private RecipeController sut;

    @BeforeEach
    public void setup() {

        repo = Mockito.mock(RecipeRepository.class);
        recipeService = Mockito.mock(RecipeService.class);
        ingredientRepo = Mockito.mock(IngredientRepository.class);
        stepRepo = Mockito.mock(StepRepository.class);
        msgs = Mockito.mock(SimpMessagingTemplate.class);

        sut = new RecipeController(repo, recipeService, ingredientRepo, stepRepo, msgs);
    }

    @Test
    public void addRecipeBroadcastsEvent() {
        Recipe recipe = new Recipe("Pizza", new ArrayList<>(), new ArrayList<>());
        Recipe savedRecipe = new Recipe("Pizza", new ArrayList<>(), new ArrayList<>());
        savedRecipe.setId(1L);

        when(repo.save(recipe)).thenReturn(savedRecipe);

        sut.add(recipe);

        verify(repo).save(recipe);

        verify(msgs).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }

    @Test
    public void deleteRecipeBroadcastsEvent() {
        long id = 5L;
        when(repo.existsById(id)).thenReturn(true);

        var response = sut.deleteRecipe(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(repo).deleteById(id);


        verify(msgs).convertAndSend(eq("/topic/recipes"), any(RecipeEvent.class));
    }

    @Test
    public void updateRecipeBroadcastsEvent() {
        long id = 1L;
        Recipe existing = new Recipe("Old Name", new ArrayList<>(), new ArrayList<>());
        existing.setId(id);

        Recipe updateInfo = new Recipe("New Name", null, null);

        when(repo.findById(id)).thenReturn(Optional.of(existing));
        when(repo.save(any(Recipe.class))).thenReturn(existing);

        sut.updateRecipe(id, updateInfo);

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
    public void deleteIngredientBroadcastsUpdate() {
        long recipeId = 1L;
        long riId = 2L;
        Recipe updated = new Recipe();
        updated.setId(recipeId);

        when(recipeService.deleteIngredient(recipeId, riId)).thenReturn(updated);

        sut.deleteIngredient(recipeId, riId);

        verify(msgs).convertAndSend(eq("/topic/recipes/" + recipeId), eq(updated));
    }
}