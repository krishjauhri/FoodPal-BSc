package client.utils;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;

public class NutritionService {
    public double calculateRecipeKal(Recipe recipe) {
        if (recipe == null || recipe.getIngredients() == null || recipe.getIngredients().isEmpty()) {
            return 0;
        }

        double totalKcal = 0;
        double totalWeight = 0;

        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri == null) continue;

            Ingredient ingredient = ri.getIngredient();
            if (ingredient == null) continue;

            if (!isMassUnit(ri.getUnit())) continue;

            double grams = toGrams(ri.getAmount(), ri.getUnit());
            double kcalPer100g = ingredient.calculateKcalPer100g();

            totalKcal += kcalPer100g * (grams / 100.0);
            totalWeight += grams;
        }

        if (totalWeight == 0) return 0;
        return totalKcal / totalWeight * 100;
    }

    public int inferServingsIfNeeded(Recipe recipe) {
        if (recipe == null) return 1;

        if (recipe.getServings() > 0) {
            return recipe.getServings();
        }

        if (recipe.getIngredients() == null) return 1;

        double grams = 0;
        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri == null || ri.getIngredient() == null) continue;
            if (!isMassUnit(ri.getUnit())) continue;
            grams += toGrams(ri.getAmount(), ri.getUnit());
        }

        int inferred = (int) Math.round(grams / 250.0);
        return Math.max(1, inferred);
    }

    public NutritionScaled calculateTotalKcalScaled(Recipe recipe, double factor) {
        if (recipe == null || recipe.getIngredients() == null) {
            return new NutritionScaled(0, 0, 0, 0, 0);
        }

        if (!Double.isFinite(factor) || factor <= 0) factor = 1.0;

        double gramsTotal = 0;
        double proteinTotal = 0;
        double fatTotal = 0;
        double carbsTotal = 0;

        for (RecipeIngredient ri : recipe.getIngredients()) {
            if (ri == null || ri.getIngredient() == null) continue;
            if (!isMassUnit(ri.getUnit())) continue;

            double grams = toGrams(ri.getAmount(), ri.getUnit()) * factor;
            gramsTotal += grams;

            Ingredient ing = ri.getIngredient();
            proteinTotal += ing.getProtein() * (grams / 100.0);
            fatTotal     += ing.getFat()     * (grams / 100.0);
            carbsTotal   += ing.getCarbs()   * (grams / 100.0);
        }

        double kcalTotal = 4 * proteinTotal + 9 * fatTotal + 4 * carbsTotal;
        return new NutritionScaled(kcalTotal, proteinTotal, fatTotal, carbsTotal, gramsTotal);
    }

    /* ===================== HELPERS ===================== */

    private boolean isMassUnit(String unit) {
        if (unit == null) return false;
        String u = unit.trim().toLowerCase();
        return u.equals("g") || u.equals("kg");
    }

    private double toGrams(double amount, String unit) {
        if (unit == null) return 0;
        return unit.trim().equalsIgnoreCase("kg")
                ? amount * 1000.0
                : amount;
    }

    public record NutritionScaled(
            double kcal,
            double protein,
            double fat,
            double carbs,
            double grams
    ) {}
}
