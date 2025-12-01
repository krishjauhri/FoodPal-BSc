package server.service;

import commons.Recipe;
import commons.RecipeIngredient;
import org.springframework.stereotype.Service;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    public Recipe addIngredient(Long recipeId, RecipeIngredient ingredient){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        ingredient.setRecipe(recipe);
        ingredientRepository.save(ingredient);

        recipe.getIngredients().add(ingredient);
        return recipeRepository.save(recipe);
    }

    public Recipe updateIngredient(Long recipeId, Long recipeIngredientId, double amount, String unit){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        RecipeIngredient ri = ingredientRepository.findById(recipeIngredientId)
                .orElseThrow(() -> new RuntimeException("RecipeIngredient not found"));

        ri.setAmount(amount);
        ri.setUnit(unit);
        ingredientRepository.save(ri);
        return recipe;
    }

    public Recipe deleteIngredient(Long recipeId, Long recipeIngredientId){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        boolean foundAndRemoved = recipe.getIngredients().removeIf(ri -> {
            // ri.getId() will generally not trigger lazy-loading issues.
            return ri.getId() == recipeIngredientId;
        });

        if (!foundAndRemoved) {
            // Still throw a 400 if the ingredient ID doesn't belong to the recipe.
            throw new RuntimeException("RecipeIngredient with ID " + recipeIngredientId + " is not associated with Recipe with ID " + recipeId + ".");
        }

        // The ingredient entity itself will be deleted by JPA via orphanRemoval=true
        // when the parent Recipe entity is saved.
        return recipeRepository.save(recipe);
    }

}
