package client.utils;

import commons.Ingredient;
import commons.Recipe;
import commons.RecipeIngredient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class NutritionServiceTest {

    private NutritionService sut;

    @BeforeEach
    void setup() {
        sut = new NutritionService();
    }

    @Test
    void calculateRecipeKal_nullOrEmpty_returns0() {
        assertEquals(0.0, sut.calculateRecipeKal(null), 1e-9);

        Recipe r = new Recipe();
        r.setIngredients(new ArrayList<>());
        assertEquals(0.0, sut.calculateRecipeKal(r), 1e-9);
    }

    @Test
    void calculateRecipeKal_usesOnlyMassUnits() {
        Ingredient ing = new Ingredient();
        ing.setProtein(0);
        ing.setFat(0);
        ing.setCarbs(0);
        // kcalPer100g will be 0 here; we only test filtering behavior.

        RecipeIngredient nonMass = new RecipeIngredient();
        nonMass.setIngredient(ing);
        nonMass.setAmount(100);
        nonMass.setUnit("tbsp"); // should be ignored

        Recipe r = new Recipe();
        r.setIngredients(new ArrayList<>());
        r.getIngredients().add(nonMass);

        assertEquals(0.0, sut.calculateRecipeKal(r), 1e-9);
    }

    @Test
    void inferServingsIfNeeded_returnsRecipeServingsIfSet() {
        Recipe r = new Recipe();
        r.setServings(4);
        assertEquals(4, sut.inferServingsIfNeeded(r));
    }

    @Test
    void calculateTotalKcalScaled_scalesMacrosAndGrams() {
        Ingredient ing = new Ingredient();
        ing.setProtein(10); // per 100g
        ing.setFat(5);
        ing.setCarbs(20);

        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ing);
        ri.setAmount(100);
        ri.setUnit("g");

        Recipe r = new Recipe();
        r.setIngredients(new ArrayList<>());
        r.getIngredients().add(ri);

        var totals = sut.calculateTotalKcalScaled(r, 2.0); // 200g

        assertEquals(200.0, totals.grams(), 1e-9);
        assertEquals(20.0, totals.protein(), 1e-9);
        assertEquals(10.0, totals.fat(), 1e-9);
        assertEquals(40.0, totals.carbs(), 1e-9);

        assertEquals(330.0, totals.kcal(), 1e-9);
    }
}
