package client.utils;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;
import commons.Ingredient;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeSearchServiceTest {

    private final RecipeSearchService sut = new RecipeSearchService();
    private Recipe recipe(String name, List<RecipeIngredient> ingredients, List<Step> steps) {
        Recipe r = new Recipe();
        r.setName(name);
        r.setIngredients(ingredients);
        r.setSteps(steps);
        return r;
    }

    private List<RecipeIngredient> ingredients(String... names) {
        List<RecipeIngredient> list = new ArrayList<>();
        for (String n : names) {
            list.add(recipeIngredient(ingredient(n)));
        }
        return list;
    }

    private Ingredient ingredient(String name) {
        Ingredient i = new Ingredient();
        i.setName(name);
        return i;
    }

    private RecipeIngredient recipeIngredient(Ingredient ingredient) {
        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredient(ingredient);
        return ri;
    }

    private List<Step> steps(String... texts) {
        List<Step> list = new ArrayList<>();
        for (String t : texts) {
            list.add(step(t));
        }
        return list;
    }

    private Step step(String text) {
        Step s = new Step();
        s.setText(text);
        return s;
    }

    @Test
    void filter_nullOrBlankQuery_returnsCopyOfAllRecipes() {
        List<Recipe> all = Arrays.asList(
                recipe("Pasta", null, null),
                recipe("Salad", null, null)
        );

        List<Recipe> r1 = sut.filter(all, null);
        assertIterableEquals(all, r1);
        assertNotSame(all, r1);

        List<Recipe> r2 = sut.filter(all, "   ");
        assertIterableEquals(all, r2);
        assertNotSame(all, r2);
    }

    @Test
    void filter_isCaseInsensitive_andTrims() {
        Recipe a = recipe("Spicy Chili", null, null);
        Recipe b = recipe("Sweet Pancakes", null, null);

        List<Recipe> result = sut.filter(Arrays.asList(a, b), "  CHILI ");
        assertEquals(1, result.size());
        assertSame(a, result.get(0));
    }

    @Test
    void filter_matchesAcrossNameIngredientsAndSteps() {
        Recipe byName = recipe("Chicken Curry", null, null);
        Recipe byIngredient = recipe("Anything", ingredients("Garlic", "Onion"), null);
        Recipe byStep = recipe("A", null, steps("Chop onions", "Fry garlic"));

        List<Recipe> all = Arrays.asList(byName, byIngredient, byStep);

        assertSame(byName, sut.filter(all, "curry").get(0));
        assertSame(byIngredient, sut.filter(all, "onion").get(0));
        assertSame(byStep, sut.filter(all, "fry").get(0));
    }

    @Test
    void filter_multipleTerms_areANDed() {
        Recipe r1 = recipe("Chicken Curry", ingredients("Onion"), steps("Serve hot"));
        Recipe r2 = recipe("Chicken Soup", ingredients("Onion"), steps("Serve cold"));
        Recipe r3 = recipe("Curry", ingredients("Rice"), steps("Serve hot"));

        List<Recipe> result = sut.filter(Arrays.asList(r1, r2, r3), "chicken hot");
        assertEquals(1, result.size());
        assertSame(r1, result.get(0));
    }

    @Test
    void filter_handlesNullFieldsSafely() {
        Recipe r1 = recipe(null, null, null);

        List<RecipeIngredient> ing = new ArrayList<>();
        ing.add(null);
        ing.add(recipeIngredient(null));
        ing.add(recipeIngredient(ingredient(null)));

        List<Step> st = new ArrayList<>();
        st.add(null);
        st.add(step(null));

        Recipe r2 = recipe("Hello", ing, st);

        assertDoesNotThrow(() -> sut.filter(Arrays.asList(r1, r2), "hello"));

        List<Recipe> result = sut.filter(Arrays.asList(r1, r2), "hello");
        assertEquals(1, result.size());
        assertSame(r2, result.get(0));
    }

}
