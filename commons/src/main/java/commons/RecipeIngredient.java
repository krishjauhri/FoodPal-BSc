package commons;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class RecipeIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = true)
    @JsonBackReference
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    @ManyToOne
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient;

    private double amount;
    private String unit;

    public RecipeIngredient(){

    }
    /**
     * Constructor for tests
     * */
    public RecipeIngredient(Ingredient ingredient, double amount, String unit) {
        this.ingredient = ingredient;
        setAmount(amount);
        setUnit(unit);
    }

    /**
     * Constructor for Production code
     * */
    public RecipeIngredient(Recipe recipe, Ingredient ingredient,
                            double amount, String unit) {
       this.recipe = recipe;
       this.ingredient = ingredient;
       setAmount(amount);
       setUnit(unit);
    }

    public long getId() {return id;}

    public Recipe getRecipe() {return recipe;}

    public void setRecipe(Recipe recipe) {this.recipe = recipe;}

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if(!Double.isFinite(amount) || amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        if(unit == null) {
            throw new IllegalArgumentException("Unit must not be null.");
        }
        String u = unit.trim();
        if(u.isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty.");
        }
        this.unit = u;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RecipeIngredient that = (RecipeIngredient) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
//        if (ingredient == null) return "Unknown ingredient";
//        return amount + " " + unit + " " + ingredient.getName();
        return displayAmount();
    }

    public String displayAmount() {
        double displayAmount = amount;
        String displayUnit = unit;

        if (unit.equals("g") && amount >= 1000) {
            displayAmount = amount / 1000.0;
            displayUnit = "kg";

        } else if (unit.equals("ml") && amount >= 1000) {
            displayAmount = amount / 1000.0;
            displayUnit = "l";
        }

        return formatAmount(displayAmount) + " " + displayUnit + " " + ingredient.getName();

    }
    private String formatAmount(double value) {
        if (value == Math.floor(value)) {
            return String.valueOf((int) value);
        }
        return String.valueOf(value);
    }



}
