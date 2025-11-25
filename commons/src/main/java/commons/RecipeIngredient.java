package commons;

import java.util.Objects;

public class RecipeIngredient {

    private Ingredient ingredient;
    private double amount;
    private String unit;

    public RecipeIngredient(){

    }
    public RecipeIngredient(Ingredient ingredient, double amount, String unit) {
        if(amount <= 0){
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.ingredient = ingredient;
        this.amount = amount;
        this.unit = unit;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public double getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public void setAmount(double amount) {
        if(amount <= 0){
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.amount = amount;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RecipeIngredient that = (RecipeIngredient) o;
        return Double.compare(amount, that.amount) == 0 && Objects.equals(ingredient, that.ingredient) && Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount, unit);
    }
}
