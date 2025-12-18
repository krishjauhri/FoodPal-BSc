package commons;

public class RecipeEvent {
    public enum Type{
        ADD,
        DELETE,
        UPDATE
    }

    public Type type;
    public long id;
    public String name;

    public RecipeEvent() {}

    public RecipeEvent(Type type, long id, String name) {
        this.type = type;
        this.id = id;
        this.name = name;
    }
}
