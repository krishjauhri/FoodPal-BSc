package server.api;

import commons.ShoppingItem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import server.service.ShoppingItemService;

import java.util.List;

@RestController
@RequestMapping("/api/shopping-list")
public class ShoppingItemController {

    private final ShoppingItemService service;

    public ShoppingItemController(ShoppingItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ShoppingItem> getAll() {
        return service.getAllItems();
    }

    @PostMapping
    public ResponseEntity<ShoppingItem> addItem(@RequestBody ShoppingItem item) {
        ShoppingItem saved = service.addShoppingItem(item);
        return ResponseEntity.ok(saved);
    }

    @PutMapping
    public ResponseEntity<ShoppingItem> updateItem(@RequestBody ShoppingItem item) {
        ShoppingItem updated = service.updateShoppingItem(item);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        try {
            service.deleteShoppingItem(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}