package server.service;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.springframework.stereotype.Service;
import server.database.IngredientRepository;
import server.database.RecipeRepository;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             RecipeRepository recipeRepository) {
        this.ingredientRepository = ingredientRepository;
        this.recipeRepository = recipeRepository;
    }

    public Ingredient updateIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }

    public Ingredient addIngredient(Ingredient ingredient) {
        ingredient.setId(null);
        return ingredientRepository.save(ingredient);
    }

    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }

    public void deleteIngredientInRecipe(Long ingredientId) {
        Ingredient ingredient = ingredientRepository
                .findById(ingredientId)
                .orElseThrow();

        List<Recipe> recipes = recipeRepository.findAll();

        for (Recipe recipe : recipes) {

            List<RecipeIngredient> toDelete = new ArrayList<>();

            for (RecipeIngredient ri : recipe.getIngredients()) {
                if (ri.getIngredient().getId().equals(ingredientId)) {
                    toDelete.add(ri);
                }
            }
            recipe.getIngredients().removeAll(toDelete);
            recipeRepository.save(recipe);
        }
        ingredientRepository.delete(ingredient);
    }


}
