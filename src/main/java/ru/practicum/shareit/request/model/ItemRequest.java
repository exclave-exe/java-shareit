package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
public class ItemRequest {
    Long id;
    String description;
    User requestor;
    LocalDateTime created;
}
