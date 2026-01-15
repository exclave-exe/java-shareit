package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private Long id = 0L;
    private final Map<Long, Item> items = new HashMap<>();

    public Item getItemById(Long id) {
        Item item = items.get(id);
        return item != null ? item.toBuilder().build() : null;
    }

    public Item saveItem(Item item) {
        if (item.getId() == null) {
            item.setId(generateId());
        }
        items.put(item.getId(), item);
        return item.toBuilder().build();
    }

    public List<Item> getUserItems(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(item -> item.toBuilder().build())
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
                .map(item -> item.toBuilder().build())
                .toList();
    }

    private Long generateId() {
        return ++id;
    }
}
