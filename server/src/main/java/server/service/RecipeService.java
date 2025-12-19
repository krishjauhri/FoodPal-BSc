package server.service;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;
import org.springframework.stereotype.Service;
import server.database.RecipeIngredientRepository;
import server.database.RecipeRepository;
import server.database.StepRepository;

@Service
public class RecipeService {
    private final RecipeRepository recipeRepository;
    private final RecipeIngredientRepository ingredientRepository;
    private final StepRepository stepRepository;

    public RecipeService(RecipeRepository recipeRepository,
                         RecipeIngredientRepository ingredientRepository,
                         StepRepository stepRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.stepRepository = stepRepository;
    }

    public Recipe addIngredient(Long recipeId, RecipeIngredient ingredient){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        ingredient.setRecipe(recipe);
        ingredientRepository.save(ingredient);

        recipe.getIngredients().add(ingredient);
        return recipeRepository.save(recipe);
    }

    public Recipe updateIngredient(Long recipeId, long riId, double amount, String unit) {
        Recipe recipe =  recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        RecipeIngredient ri = recipe.getIngredients().stream()
                .filter(x -> x.getId() == riId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        ri.setAmount(amount);
        ri.setUnit(unit);

        return recipeRepository.save(recipe);
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

    public Recipe addStep(Long recipeId, Step step){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        step.setRecipe(recipe);
        stepRepository.save(step);

        recipe.getSteps().add(step);
        return recipeRepository.save(recipe);
    }

    public Recipe updateStep(Long recipeId, Long stepId, int order, String text){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        Step s = stepRepository.findById(stepId)
                .orElseThrow(() -> new RuntimeException("Step not found"));

        s.setOrder(order);
        s.setText(text);
        stepRepository.save(s);
        return recipe;
    }

    public Recipe deleteStep(Long recipeId, Long stepId){
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        boolean removed = recipe.getSteps().removeIf(s -> s.getId() == stepId);
        //if stepId doesnt match recipeId
        if (!removed) {
            throw new RuntimeException("Step with ID " + stepId + " is not associated with Recipe " + recipeId);
        }

        return recipeRepository.save(recipe);
    }

}
