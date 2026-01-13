package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreateDto {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is incorrect")
    private String email;
}
