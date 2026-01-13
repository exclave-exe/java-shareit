package ru.practicum.shareit.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;

    public User(User other) {
        this.id = other.id;
        this.name = other.name;
        this.email = other.email;
    }
}
