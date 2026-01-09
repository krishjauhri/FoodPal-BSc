package client.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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

    @Test
    void removeNonExistingFavourites() {
        sut.addFavourite(1L);
        sut.addFavourite(2L);
        sut.addFavourite(3L);

        Set<Long> existing = new java.util.HashSet<>();
        existing.add(1L);
        existing.add(3L);

        var removed = sut.removeNonExistingFavourites(existing);

        assertEquals(1, removed.size());
        assertTrue(removed.contains(2L));
        assertTrue(sut.isFavourite(1L));
        assertFalse(sut.isFavourite(2L));
        assertTrue(sut.isFavourite(3L));

        ConfigService newService = new ConfigService(tempFile);
        assertFalse(newService.isFavourite(2L));
    }
}