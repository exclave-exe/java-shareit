package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {

    Item getItemById(Long id);

    Item saveItem(Item item);

    List<Item> getUserItems(Long userId);

    List<Item> searchItems(String searchQuery);
}
