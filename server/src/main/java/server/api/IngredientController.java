package server.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.IngredientService;
import commons.Ingredient;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @PutMapping
    public Ingredient updateIngredient(@RequestBody Ingredient ingredient) {
        return ingredientService.updateIngredient(ingredient);
    }

    @PostMapping
    public Ingredient addIngredient(@RequestBody Ingredient ingredient) {
        return ingredientService.addIngredient(ingredient);
    }

    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return ingredientService.getAllIngredients();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        boolean deleted = ingredientService.deleteIngredientEverywhere(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> updateIngredientById(@PathVariable Long id,
                                                           @RequestBody Ingredient ingredient) {
        Ingredient updated = ingredientService.updateIngredientById(id, ingredient);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

}
