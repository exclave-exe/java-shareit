package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private static Long id = 0L;
    HashMap<Long, Item> items = new HashMap<>();

    public Item getItemById(Long id) {
        return items.get(id);
    }

    public Item saveItem(Item item) {
        if (item.getId() == null) {
            item.setId(generateId());
        }
        items.put(item.getId(), item);
        return new Item(item);
    }

    public List<Item> getUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .toList();
    }

    @Override
    public List<Item> searchItems(String searchQuery) {
        if (searchQuery == null || searchQuery.isBlank()) {
            return Collections.emptyList();
        }

        String lowerQuery = searchQuery.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item -> item.getName().toLowerCase().contains(lowerQuery) ||
                        item.getDescription().toLowerCase().contains(lowerQuery))
                .toList();
    }

    private Long generateId() {
        return ++id;
    }
}
