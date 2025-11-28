package commons;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


import java.util.Objects;

@Entity
public class Shoppingitem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String ingredientName;
    private double amount;
    private String unit;

    //The name of the recipe where the item came from
    private String sourceRecipe;

    @SuppressWarnings("unused")
    private Shoppingitem() {

    }



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
