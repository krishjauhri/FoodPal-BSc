package server.api;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;
import commons.RecipeEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.IngredientService;
import server.service.RecipeService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final SimpMessagingTemplate message;
    private final IngredientService ingredientService;

    public RecipeController(RecipeService recipeService,
                            IngredientService ingredientService,
                            SimpMessagingTemplate message) {
        this.recipeService = recipeService;
        this.ingredientService = ingredientService;
        this.message = message;
    }

    @PostMapping
    public Recipe add(@RequestBody Recipe recipe) {
        Recipe savedRecipe = recipeService.addRecipe(recipe);
        message.convertAndSend("/topic/recipes", new RecipeEvent(RecipeEvent.Type.ADD,
                savedRecipe.getId(), savedRecipe.getName()));
        return savedRecipe;
    }

    @GetMapping
    public List<Recipe> getAll() {
        return recipeService.getAllRecipes();
    }

    @PostMapping("/{recipeId}/ingredients")
    public ResponseEntity<Recipe> addIngredientToRecipe(
            @PathVariable Long recipeId,
            @RequestBody RecipeIngredient ingredient) {
        try {
            Recipe updated = recipeService.addIngredient(recipeId, ingredient);

            //for websocket
            message.convertAndSend("/topic/recipes/" + recipeId, updated);
            return ResponseEntity.ok(updated);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{recipeId}/steps")
    public ResponseEntity<Recipe> addStepToRecipe(
            @PathVariable Long recipeId,
            @RequestBody Step step) {

        Recipe updated = recipeService.addStep(recipeId, step);

        //websocket
        message.convertAndSend("/topic/recipes/" + recipeId, updated);
        return ResponseEntity.ok(updated);
    }



    @PostMapping("/ingredients")
    public Ingredient createIngredient(@RequestBody Ingredient ingredient) {
        return ingredientService.createIngredient(ingredient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable long id,
                                               @RequestBody Recipe updated) {

        Recipe savedRecipe = recipeService.updateRecipeName(id, updated.getName());
        if (savedRecipe == null) {
            return ResponseEntity.notFound().build();
        }

        message.convertAndSend("/topic/recipes", new RecipeEvent(RecipeEvent.Type.UPDATE,
                savedRecipe.getId(), savedRecipe.getName()));
        message.convertAndSend("/topic/recipes/" + id, savedRecipe);

        return ResponseEntity.ok(savedRecipe);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable long id) {
        boolean deleted = recipeService.deleteRecipe(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        message.convertAndSend("/topic/recipes", new RecipeEvent(RecipeEvent.Type.DELETE, id, null));
        return ResponseEntity.noContent().build();
    }


    @PutMapping("/{recipeId}/ingredients/{riId}")
    public ResponseEntity<Recipe> updateIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long riId,
            @RequestBody UpdateIngredientRequest req){
        try {
            Recipe updated = recipeService.updateIngredient(recipeId, riId, req.amount, req.unit);

            //websocket
            message.convertAndSend("/topic/recipes/" + recipeId, updated);
            return ResponseEntity.ok(updated);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    public record UpdateIngredientRequest(double amount, String unit){}

    @PutMapping("/{recipeId}/steps/{stepId}")
    public ResponseEntity<Recipe> updateStep(
            @PathVariable Long recipeId,
            @PathVariable Long stepId,
            @RequestBody UpdateStepRequest req) {

        Recipe updated = recipeService.updateStep(recipeId, stepId, req.order, req.text);

        //websocket
        message.convertAndSend("/topic/recipes/" + recipeId, updated);
        return ResponseEntity.ok(updated);
    }
    public record UpdateStepRequest(int order, String text) {}

    @DeleteMapping("/{recipeId}/ingredients/{riId}")
    public ResponseEntity<Recipe> deleteIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long riId){

        try {
            Recipe updated = recipeService.deleteIngredient(recipeId, riId);

            //websocket
            message.convertAndSend("/topic/recipes/" + recipeId, updated);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{recipeId}/steps/{stepId}")
    public ResponseEntity<Recipe> deleteStep(
            @PathVariable Long recipeId,
            @PathVariable Long stepId) {

        try {
            Recipe updated = recipeService.deleteStep(recipeId, stepId);

            //websockets
            message.convertAndSend("/topic/recipes/" + recipeId, updated);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/servings")
    public Recipe updateServings(@PathVariable long id, @RequestBody int servings) {
        return recipeService.updateServings(id, servings);
    }

}
