package client.utils;

import commons.Recipe;
import commons.RecipeIngredient;
import commons.Step;

import java.util.ArrayList;
import java.util.List;

public class RecipeSearchService {

    public List<Recipe> filter(List<Recipe> allRecipes, String query) {
        String q = "";
        if (query != null) {
            q = query.trim().toLowerCase();
        }

        if (q.length() == 0) {
            return new ArrayList<Recipe>(allRecipes);
        }

        String[] terms = q.split("\\s+"); // "t1 t2 t3" = AND

        List<Recipe> filtered = new ArrayList<Recipe>();
        for (int i = 0; i < allRecipes.size(); i++) {
            Recipe r = allRecipes.get(i);
            if (matchesAllTerms(r, terms)) {
                filtered.add(r);
            }
        }
        return filtered;
    }

    private boolean matchesAllTerms(Recipe r, String[] terms) {
        String haystack = buildSearchText(r);

        for (int i = 0; i < terms.length; i++) {
            String t = terms[i];
            if (t == null || t.length() == 0) continue;
            if (haystack.indexOf(t) < 0) return false;
        }
        return true;
    }

    private String buildSearchText(Recipe r) {
        StringBuilder sb = new StringBuilder();

        if (r != null && r.getName() != null) {
            sb.append(r.getName()).append(" ");
        }

        if (r != null && r.getIngredients() != null) {
            for (int i = 0; i < r.getIngredients().size(); i++) {
                RecipeIngredient ri = r.getIngredients().get(i);
                if (ri != null && ri.getIngredient() != null && ri.getIngredient().getName() != null) {
                    sb.append(ri.getIngredient().getName()).append(" ");
                }
            }
        }

        if (r != null && r.getSteps() != null) {
            for (int i = 0; i < r.getSteps().size(); i++) {
                Step s = r.getSteps().get(i);
                if (s != null && s.getText() != null) {
                    sb.append(s.getText()).append(" ");
                }
            }
        }

        return sb.toString().toLowerCase();
    }
}

