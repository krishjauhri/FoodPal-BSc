package client.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;

public class ConfigService {
    private static final String configFile = "foodpal_config.json";
    private final ObjectMapper mapper;
    private ClientConfig config;
    private File file;

    @Inject
    public ConfigService() {
        this.mapper = new ObjectMapper();
        this.file = new File(configFile);
        loadConfig();
    }

    public ConfigService(File file){
        this.mapper = new ObjectMapper();
        this.file = file;
        loadConfig();
    }

    private void loadConfig(){
        if(file.exists()){
            try {
                config = mapper.readValue(file, ClientConfig.class);
            } catch (IOException e) {
                this.config = new ClientConfig();
            }
        }else{
            this.config = new ClientConfig();
        }
    }

    private void saveConfig(){
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
        } catch (IOException e) {
            System.err.println("Failed to save config: " + e.getMessage());

        }
    }
    public boolean isFavourite(long recipeId){
        return config.getFavouriteRecipes().contains(recipeId);
    }
    public void addFavourite(long recipeId){
        if(!isFavourite(recipeId)){
            config.getFavouriteRecipes().add(recipeId);
            saveConfig();
        }
    }
    public void removeFavourite(long recipeId){
        if(isFavourite(recipeId)){
            config.getFavouriteRecipes().remove(recipeId);
            saveConfig();
        }
    }
    public boolean toggleFavorite(long recipeId){
        if(isFavourite(recipeId)){
            removeFavourite(recipeId);
            return false;
        }else{
            addFavourite(recipeId);
            return true;
        }
    }
    public ClientConfig getConfig(){
        return config;
    }

}
