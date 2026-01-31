package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

public interface UserRepository {

    User getUserById(Long id);

    User saveUser(User user);

    void deleteUserById(Long id);

    boolean existsByEmail(String email);
}
