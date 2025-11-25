package commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {

    private long id;
    private String name;
    private List<Recipe> recipes = new ArrayList<>();

    public User() {}

    public User(long id, String name, List<Recipe> recipes) {
        this.id = id;
        this.name = name;
        this.recipes = recipes;
    }

    public boolean addRecipe(Recipe recipe) {
        if (recipe == null) return false;
        if (!recipes.contains(recipe)) {
            recipes.add(recipe);
            return true;
        }
        return false;
    }

    public boolean deleteRecipe(Recipe recipe) {
        if (recipe == null) return false;
        return recipes.remove(recipe);
    }

    public Recipe getRecipeByName(String recipeName) {
        if (recipeName == null) return null;
        for (Recipe r : recipes) {
            if (recipeName.equals(r.getName())) {
                return r;
            }
        }
        return null;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }


    public List<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<Recipe> recipes) {
        this.recipes = recipes;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getId() == user.getId() && Objects.equals(getName(), user.getName()) && Objects.equals(getRecipes(), user.getRecipes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getRecipes());
    }
}
