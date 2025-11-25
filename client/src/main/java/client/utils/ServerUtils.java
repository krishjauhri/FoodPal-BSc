package client.utils;

import commons.Recipe;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class ServerUtils {

    private static final String SERVER = "http://localhost:8080/";

    public Recipe addRecipe(Recipe recipe) {
        return ClientBuilder.newClient()
                .target(SERVER).path("api/recipes")
                .request(APPLICATION_JSON)
                .post(Entity.entity(recipe, APPLICATION_JSON), Recipe.class);
    }

    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient()
                    .target(SERVER)
                    .request(APPLICATION_JSON)
                    .get();
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
