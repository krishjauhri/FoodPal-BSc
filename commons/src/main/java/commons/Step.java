package commons;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "step_order")
    private int stepOrder;
    private String text;

    @ManyToOne(optional = true)
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;

    public Step(){
    }

    public Step(int order, String text) {
        this.stepOrder = order;
        this.text = text;
    }
    public Step(Recipe recipe,int order, String text){
        this.recipe = recipe;
        this.stepOrder = order;
        this.text = text;;
    }

    public Recipe getRecipe() {return recipe;}

    public void setRecipe(Recipe recipe) {this.recipe = recipe;}

    public long getId() {return id;}

    public int getOrder() {
        return stepOrder;
    }

    public void setOrder(int order) {
        this.stepOrder = order;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Step step = (Step) object;
        return id == step.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
