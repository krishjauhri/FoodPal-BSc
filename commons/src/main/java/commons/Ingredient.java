package commons;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private double protein;
    private double fat;
    private double carbs;


    public Ingredient() {}

    public Ingredient(String name, double protein, double fat, double carbs) {
        this.name = name;
        this.protein = protein;
        this.fat = fat;
        this.carbs = carbs;
    }
    public long getId() {return id;}

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getProtein() {
        return protein;
    }

    public void setProtein(double protein) {
        this.protein = protein;
    }

    public double getFat() {
        return fat;
    }

    public void setFat(double fat) {
        this.fat = fat;
    }

    public double getCarbs() {
        return carbs;
    }

    public void setCarbs(double carbs) {
        this.carbs = carbs;
    }

    /**
     * Calculates the amount of Kcal per 100 grams based of macronutrients
     * @return the estimated Kcal per 100 grams.
     */
    public double calculateKcalPer100g(){
        return (this.protein * 4) + (this.carbs * 4) + (this.fat * 9);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Double.compare(protein, that.protein) == 0 && Double.compare(fat, that.fat) == 0 && Double.compare(carbs, that.carbs) == 0 && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash( name, protein, fat, carbs);
    }
}
