package commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Recipe {

    private long id;
    private String name;
    private List<Ingredient> ingredients = new ArrayList<>();
    private List<Step> steps = new ArrayList<>();

    public Recipe() {
    }

    public Recipe(String name, List<Ingredient> ingredients, List<Step> steps) {
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

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        if(ingredients != null)
            this.ingredients = ingredients;
        else
            this.ingredients = new ArrayList<>();
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        if(steps != null)
            this.steps = steps;
        else
            this.steps = new ArrayList<>();
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Recipe recipe = (Recipe) object;
        return Objects.equals(getName(), recipe.getName()) && Objects.equals(getIngredients(), recipe.getIngredients()) && Objects.equals(getSteps(), recipe.getSteps());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getIngredients(), getSteps());
    }

    public boolean addIngredient(Ingredient ingredient){
        if(ingredient != null){
            return ingredients.add(ingredient);
        }
        return false;
    }

    public boolean removeIngredient(Ingredient ingredient){
        return ingredients.remove(ingredient);
    }

    public boolean addStep(Step step){
        if(step != null){
            return steps.add(step);
        }
        return false;
    }

    public boolean removeStep(Step step){
        return steps.remove(step);
    }
}
