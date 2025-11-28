package commons;

import java.util.Objects;

public class Shoppingitem {
    private String ingredientName;
    private double amount;
    private String unit;

    //The name of the recipe where the item came from
    private String sourceRecipe;

    public Shoppingitem(String ingredientName, double amount, String unit, String sourceRecipe) {
        this.ingredientName = ingredientName;
        this.amount = amount;
        this.unit = unit;
        this.sourceRecipe = sourceRecipe;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public double getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    public String getSourceRecipe() {
        return sourceRecipe;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setSourceRecipe(String sourceRecipe) {
        this.sourceRecipe = sourceRecipe;
    }
    @Override
    public String toString() {
        return getIngredientName();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Shoppingitem that = (Shoppingitem) o;
        return Double.compare(amount, that.amount) == 0 &&
                Objects.equals(ingredientName, that.ingredientName) && Objects.equals(unit, that.unit) &&
                Objects.equals(sourceRecipe, that.sourceRecipe);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredientName, amount, unit, sourceRecipe);
    }
}
