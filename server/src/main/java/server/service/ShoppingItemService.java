package server.service;

import commons.ShoppingItem;
import org.springframework.stereotype.Service;
import server.database.ShoppingItemRepository;
import java.util.List;

@Service
public class ShoppingItemService {
    private final ShoppingItemRepository repo;

    public ShoppingItemService(ShoppingItemRepository repo) {
        this.repo = repo;
    }

    public ShoppingItem addShoppingItem(ShoppingItem item) {
        item.setId(null);
        return repo.save(item);
    }

    public List<ShoppingItem> getAllItems() {
        return repo.findAll();
    }

    public ShoppingItem updateShoppingItem(ShoppingItem item) {
        return repo.save(item);
    }

    public void deleteShoppingItem(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
        } else {
            throw new IllegalArgumentException("Item with id " + id + " does not exist");
        }
    }
}
