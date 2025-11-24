package commons;

import java.util.Objects;

public class Ingredient {

    private String name;
    private int amount;
    private String unit;

    public Ingredient() {}

    public Ingredient(String name, int amount, String unit) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return getAmount() == that.getAmount() && Objects.equals(getName(), that.getName()) && Objects.equals(getUnit(), that.getUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getAmount(), getUnit());
    }


}
