package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestResponseWithItems {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<Item> items;
}
