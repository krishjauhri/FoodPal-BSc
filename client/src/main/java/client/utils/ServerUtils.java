/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import commons.Ingredient;
import commons.Step;
import commons.RecipeIngredient;
import org.glassfish.jersey.client.ClientConfig;

import commons.Quote;
import commons.Recipe;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";
    private final List<Ingredient> knownIngredients = new java.util.ArrayList<>();

    public void getQuotesTheHardWay() throws IOException, URISyntaxException {
		var url = new URI("http://localhost:8080/api/quotes").toURL();
		var is = url.openConnection().getInputStream();
		var br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
	}

	public List<Quote> getQuotes() {
		return ClientBuilder.newClient(new ClientConfig()) //
				.target(SERVER).path("api/quotes") //
				.request(APPLICATION_JSON) //
				.get(new GenericType<List<Quote>>() {});
	}

	public Quote addQuote(Quote quote) {
		return ClientBuilder.newClient(new ClientConfig()) //
				.target(SERVER).path("api/quotes") //
				.request(APPLICATION_JSON) //
				.post(Entity.entity(quote, APPLICATION_JSON), Quote.class);
	}

    public void deleteIngredient(long ingredientId) {
        ClientBuilder
                .newClient(new ClientConfig())
                .target(SERVER)
                .path("api/ingredients/" + ingredientId)
                .request(APPLICATION_JSON)
                .delete();
    }



    public List<Recipe> getRecipes() {
        List<Recipe> recipes = ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/recipes")
                .request(APPLICATION_JSON)
                .get(new GenericType<List<Recipe>>() {});

        normalizeIngredients(recipes);
        return recipes;
    }

    public Recipe addRecipe(Recipe recipe) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/recipes")
                .request(APPLICATION_JSON)
                .post(Entity.entity(recipe, APPLICATION_JSON), Recipe.class);
    }
    public Recipe updateRecipe(long id, Recipe updated) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + id)
                .request(APPLICATION_JSON)
                .put(Entity.entity(updated, APPLICATION_JSON), Recipe.class);
    }

    public void deleteRecipe(long id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + id)
                .request(APPLICATION_JSON)
                .delete();
    }

	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig()) //
					.target(SERVER) //
					.request(APPLICATION_JSON) //
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}

    public Ingredient createIngredient(String name, double protein, double fat, double carbs) {
        Ingredient existing = findIngredientByName(name);
        if (existing != null) {
            return existing;
        }

        Ingredient ingredient = new Ingredient(name, protein, fat, carbs);
        knownIngredients.add(ingredient);

        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/recipes/ingredients")
                .request(APPLICATION_JSON)
                .post(Entity.entity(ingredient, APPLICATION_JSON), Ingredient.class);
    }

    public Recipe addIngredient(long recipeId, long ingredientId, double amount, String unit) {

        RecipeIngredient ri = new RecipeIngredient();
        ri.setAmount(amount);
        ri.setUnit(unit);

        Ingredient ref = new Ingredient();
        ref.setId(ingredientId);
        ri.setIngredient(ref);

        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/ingredients")
                .request(APPLICATION_JSON)
                .post(Entity.entity(ri, APPLICATION_JSON), Recipe.class);
    }

    public Recipe addStep(long recipeId, int order, String text) {

        Step step = new Step();
        step.setOrder(order);
        step.setText(text);

        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/steps")
                .request(APPLICATION_JSON)
                .post(Entity.entity(step, APPLICATION_JSON), Recipe.class);
    }


    public static class UpdateIngredientRequest {
        public double amount;
        public String unit;

        public UpdateIngredientRequest(double amount, String unit) {
            this.amount = amount;
            this.unit = unit;
        }
    }

    public Recipe updateIngredient(long recipeId, long recipeIngredientId,
                                   double amount, String unit) {

        UpdateIngredientRequest body = new UpdateIngredientRequest(amount, unit);

        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/ingredients/" + recipeIngredientId)
                .request(APPLICATION_JSON)
                .put(Entity.entity(body, APPLICATION_JSON), Recipe.class);
    }

    public Ingredient updateIngredient(Ingredient ingredient) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("/api/ingredients")
                .request(APPLICATION_JSON)
                .put(Entity.entity(ingredient, APPLICATION_JSON), Ingredient.class);
    }
    //Add ingredient in the Ingredient interface
    public Ingredient addIngredient(Ingredient ingredient) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("/api/ingredients")
                .request(APPLICATION_JSON)
                .post(Entity.entity(ingredient, APPLICATION_JSON), Ingredient.class);
    }

    public List<Ingredient> getIngredients() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("/api/ingredients")
                .request(APPLICATION_JSON)
                .get(new GenericType<List<Ingredient>>() {});
    }




    private Ingredient findIngredientByName(String name) {
        for (Ingredient ing : knownIngredients) {
            if (ing.getName().equalsIgnoreCase(name)) {
                return ing;
            }
        }
        return null;
    }

    private void normalizeIngredients(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            for (RecipeIngredient ri : recipe.getIngredients()) {
                Ingredient serverIngredient = ri.getIngredient();
                if (serverIngredient == null) {
                    continue;
                }

                Ingredient existing = findIngredientByName(serverIngredient.getName());

                if (existing == null) {
                    knownIngredients.add(serverIngredient);
                } else {
                    ri.setIngredient(existing);
                }
            }
        }
    }

    public static class UpdateStepRequest {
        public int order;
        public String text;

        public UpdateStepRequest(int order, String text) {
            this.order = order;
            this.text = text;
        }
    }

    public Recipe updateStep(long recipeId, long stepId, int order, String text) {
        UpdateStepRequest body = new UpdateStepRequest(order, text);

        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/steps/" + stepId)
                .request(APPLICATION_JSON)
                .put(Entity.entity(body, APPLICATION_JSON), Recipe.class);
    }

    public void deleteIngredient(long recipeId, long recipeIngredientId) {
        ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/ingredients/" + recipeIngredientId)
                .request(APPLICATION_JSON)
                .delete();
    }

    public void deleteStep(long recipeId, long stepId) {
        ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/steps/" + stepId)
                .request(APPLICATION_JSON)
                .delete();
    }

    public void updateRecipeServings(long recipeId, int servings) {
        ClientBuilder.newClient(new ClientConfig())
                .target(SERVER)
                .path("api/recipes/" + recipeId + "/servings")
                .request(APPLICATION_JSON)
                .put(Entity.entity(servings, APPLICATION_JSON));
    }

}