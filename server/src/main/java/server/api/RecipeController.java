package server.api;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;
import commons.RecipeEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.IngredientRepository;
import server.database.RecipeRepository;
import server.database.StepRepository;
import server.service.RecipeService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository repo;
    private final RecipeService recipeService;
    private final IngredientRepository ingredientRepository;
    private final StepRepository stepRepository;
    private final SimpMessagingTemplate message;

    public RecipeController(RecipeRepository repo, RecipeService recipeService,
                            IngredientRepository ingredientRepository, StepRepository stepRepository,
                            SimpMessagingTemplate message) {
        this.repo = repo;
        this.recipeService = recipeService;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
        this.message = message;
    }

    @PostMapping
    public Recipe add(@RequestBody Recipe recipe) {
        Recipe savedRecipe = repo.save(recipe);
        message.convertAndSend("/topic/recipes", new RecipeEvent(RecipeEvent.Type.ADD,
                savedRecipe.getId(), savedRecipe.getName()));
        return savedRecipe;
    }

    @GetMapping
    public List<Recipe> getAll() {
        return repo.findAll();
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
        return ingredientRepository.save(ingredient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable long id,
                                               @RequestBody Recipe updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setName(updated.getName());
                    Recipe savedRecipe = repo.save(existing);
                    message.convertAndSend("/topic/recipes", new RecipeEvent(RecipeEvent.Type.UPDATE,
                            savedRecipe.getId(), savedRecipe.getName()));
                    message.convertAndSend("/topic/recipes/" + id, savedRecipe);
                    return ResponseEntity.ok(savedRecipe);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);

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
}
