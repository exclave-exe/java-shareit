package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemUpdateDto {
    @Size(min = 1, message = "Name cannot be less than one character")
    private String name;

    @Size(min = 1, message = "Description cannot be less than one character")
    private String description;

    private Boolean available;
}
