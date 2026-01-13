package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Data
@Builder
@AllArgsConstructor
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private User owner;
    private ItemRequest request;

    public Item(Item other) {
        this.id = other.id;
        this.name = other.name;
        this.description = other.description;
        this.available = other.available;
        this.owner = new User(other.owner);
        this.request = other.request;
    }
}
