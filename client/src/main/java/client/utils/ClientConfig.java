package client.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClientConfig {
    /// This class represents the structure of the JSON file
    private List<Long> favouriteRecipe;

    public ClientConfig(){
        this.favouriteRecipe = new ArrayList<>();
    }

    public List<Long> getFavouriteRecipes() {
        return favouriteRecipe;
    }
    public void setFavouriteRecipe(List<Long> favouriteRecipe) {
        this.favouriteRecipe = favouriteRecipe;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        ClientConfig that = (ClientConfig) o;
        return Objects.equals(favouriteRecipe, that.favouriteRecipe);
    }
    @Override
    public int hashCode() {
        return Objects.hash(favouriteRecipe);
    }
}
