package ru.practicum.shareit.request.model;

import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

public class ItemRequest {
    Long id;
    String description;
    User requestor;
    LocalDateTime created;
}
