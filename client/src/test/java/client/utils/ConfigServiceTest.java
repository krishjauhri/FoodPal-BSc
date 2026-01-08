package client.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ConfigServiceTest {

    private File tempFile;
    private ConfigService sut;

    @BeforeEach
    void setUp() throws IOException {

        tempFile = File.createTempFile("test_config", ".json");
        sut = new ConfigService(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void startsEmpty() {
        assertTrue(sut.getConfig().getFavouriteRecipes().isEmpty());
    }

    @Test
    void addFavoriteSavesToFile() {
        long id = 123L;
        sut.addFavourite(id);

        assertTrue(sut.isFavourite(id));

        ConfigService newService = new ConfigService(tempFile);
        assertTrue(newService.isFavourite(id));
    }

    @Test
    void removeFavoriteUpdatesFile() {
        long id = 456L;
        sut.addFavourite(id);
        sut.removeFavourite(id);

        assertFalse(sut.isFavourite(id));

        ConfigService newService = new ConfigService(tempFile);
        assertFalse(newService.isFavourite(id));
    }

    @Test
    void toggleFavoriteWorks() {
        long id = 789L;

        boolean isFav = sut.toggleFavorite(id);
        assertTrue(isFav);
        assertTrue(sut.isFavourite(id));


        isFav = sut.toggleFavorite(id);
        assertFalse(isFav);
        assertFalse(sut.isFavourite(id));
    }
}