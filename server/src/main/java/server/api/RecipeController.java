package server.api;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.database.IngredientRepository;
import server.database.RecipeRepository;
import server.service.RecipeService;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository repo;
    private final RecipeService recipeService;
    private final IngredientRepository ingredientRepository;

    public RecipeController(RecipeRepository repo, RecipeService recipeService, IngredientRepository ingredientRepository) {
        this.repo = repo;
        this.recipeService = recipeService;
        this.ingredientRepository = ingredientRepository;
    }

    @PostMapping
    public Recipe add(@RequestBody Recipe recipe) {
        return repo.save(recipe);
    }

    @GetMapping
    public List<Recipe> getAll() {
        return repo.findAll();
    }

    @PostMapping("/{recipeId}/ingredients")
    public ResponseEntity<Recipe> addIngredientToRecipe(
            @PathVariable Long recipeId,
            @RequestBody RecipeIngredient ingredient) {

        Recipe updated = recipeService.addIngredient(recipeId, ingredient);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/ingredients")
    public Ingredient createIngredient(@RequestBody Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    @PutMapping("/{recipeId}/ingredients/{riId}")
    public ResponseEntity<Recipe> updateIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long riId,
            @RequestBody UpdateIngredientRequest req){

        Recipe updated = recipeService.updateIngredient(recipeId, riId, req.amount, req.unit);
        return ResponseEntity.ok(updated);
    }
    public record UpdateIngredientRequest(double amount, String unit){}

    @DeleteMapping("/{recipeId}/ingredients/{riId}")
    public ResponseEntity<Recipe> deleteIngredient(
            @PathVariable Long recipeId,
            @PathVariable Long riId){

        try {
            recipeService.deleteIngredient(recipeId, riId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

