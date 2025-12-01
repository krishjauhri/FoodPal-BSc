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

}
