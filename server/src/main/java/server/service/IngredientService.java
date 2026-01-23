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

    public Ingredient createIngredient(Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
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

    public boolean deleteIngredientEverywhere(Long ingredientId) {
        Ingredient ingredient = ingredientRepository.findById(ingredientId).orElse(null);
        if (ingredient == null) {
            return false;
        }

        List<Recipe> recipes = recipeRepository.findAll();

        for (Recipe recipe : recipes) {
            List<RecipeIngredient> toDelete = new ArrayList<>();

            for (RecipeIngredient ri : recipe.getIngredients()) {
                if (ri.getIngredient() != null
                        && ri.getIngredient().getId() != null
                        && ri.getIngredient().getId().equals(ingredientId)) {
                    toDelete.add(ri);
                }
            }

            if (!toDelete.isEmpty()) {
                recipe.getIngredients().removeAll(toDelete);
                recipeRepository.save(recipe);
            }
        }

        ingredientRepository.delete(ingredient);
        return true;
    }

    public void deleteIngredientInRecipe(Long ingredientId) {
        boolean deleted = deleteIngredientEverywhere(ingredientId);
        if (!deleted) {
            throw new RuntimeException("Ingredient not found");
        }
    }
    
    public Ingredient updateIngredientById(Long id, Ingredient ingredient) {
        Ingredient existing = ingredientRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        existing.setName(ingredient.getName());
        existing.setProtein(ingredient.getProtein());
        existing.setFat(ingredient.getFat());
        existing.setCarbs(ingredient.getCarbs());

        return ingredientRepository.save(existing);
    }

}
