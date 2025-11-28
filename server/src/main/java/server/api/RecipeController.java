package server.api;

import commons.Recipe;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.database.RecipeRepository;

import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeRepository repo;

    public RecipeController(RecipeRepository repo) {
        this.repo = repo;
    }

    @PostMapping
    public Recipe add(@RequestBody Recipe recipe) {
        return repo.save(recipe);
    }

    @GetMapping
    public List<Recipe> getAll() {
        return repo.findAll();
    }

}

