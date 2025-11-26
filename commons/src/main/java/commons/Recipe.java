package commons;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;

    @OneToMany()
    private List<RecipeIngredient> ingredients = new ArrayList<>();
    @OneToMany()
    private List<Step> steps = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(String name, List<RecipeIngredient> ingredients, List<Step> steps) {
        this.name = name;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RecipeIngredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<RecipeIngredient> ingredients) {
        if (ingredients != null)
            this.ingredients = ingredients;
        else
            this.ingredients = new ArrayList<>();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        if (steps != null)
            this.steps = steps;
        else
            this.steps = new ArrayList<>();
    }


    public boolean addIngredient(RecipeIngredient ingredient) {
        if (ingredient != null) {
            return ingredients.add(ingredient);
        }
        return false;
    }

    public boolean removeIngredient(RecipeIngredient ingredient) {
        return ingredients.remove(ingredient);
    }

    public boolean addStep(Step step) {
        if (step != null) {
            return steps.add(step);
        }
        return false;
    }

    public boolean removeStep(Step step) {
        return steps.remove(step);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return id == recipe.id && Objects.equals(name, recipe.name) && Objects.equals(ingredients, recipe.ingredients) && Objects.equals(steps, recipe.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, ingredients, steps);
    }
}
