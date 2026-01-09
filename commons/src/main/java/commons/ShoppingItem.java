package commons;

import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    private String ingredientName;
    private double amount;
    private String unit;

    // The name of the recipe where the item came from
    private String sourceRecipe;

    @SuppressWarnings("unused")
    private ShoppingItem() {
        // for object mappers
    }

    public ShoppingItem(String ingredientName) {
        this(ingredientName, 0, null, null);
    }

    public ShoppingItem(String ingredientName, double amount, String unit) {
        this(ingredientName, amount, unit, null);
    }

    public ShoppingItem(String ingredientName, double amount, String unit,
                        String sourceRecipe) {
        this.ingredientName = ingredientName;
        this.amount = amount;
        this.unit = unit;
        this.sourceRecipe = sourceRecipe;
    }

    public long getId() { return id; }

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
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, MULTI_LINE_STYLE);
    }
}
