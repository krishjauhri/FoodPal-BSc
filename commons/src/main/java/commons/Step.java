package commons;

import java.util.Objects;

public class Step {
    private int order;
    private String text;

    public Step(){
    }

    public Step(int order, String text){
        this.order = order;
        this.text = text;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
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
        return getOrder() == step.getOrder() && Objects.equals(getText(), step.getText());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrder(), getText());
    }
}
