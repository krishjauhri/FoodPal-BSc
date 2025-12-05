package commons;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RecipeIngredient> ingredients = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
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
        if (ingredient == null) {
            return false;
        }
        ingredients.add(ingredient);
        ingredient.setRecipe(this);
        return true;
    }

    public boolean removeIngredient(RecipeIngredient ingredient) {
        if (ingredient == null) {
            return false;
        }
        for (int i = 0; i < ingredients.size(); i++) {
            RecipeIngredient current = ingredients.get(i);
            boolean sameIngredient = Objects.equals(current.getIngredient(), ingredient.getIngredient());
            boolean sameAmount = Double.compare(current.getAmount(), ingredient.getAmount()) == 0;
            boolean sameUnit = Objects.equals(current.getUnit(), ingredient.getUnit());

            if (sameIngredient && sameAmount && sameUnit) {
                ingredients.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean addStep(Step step) {
        if (step != null) {
            return steps.add(step);
        }
        return false;
    }

    public boolean removeStep(Step step) {
        if (step == null) {
            return false;
        }
        boolean removed = steps.remove(step);
        if (removed) {
            step.setRecipe(null);
        }
        return removed;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Recipe recipe = (Recipe) o;
        return Objects.equals(name, recipe.name) && Objects.equals(ingredients, recipe.ingredients) && Objects.equals(steps, recipe.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, ingredients, steps);
    }

}
