package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private static Long id = 0L;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        return user != null ? new User(user) : null;
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId(generateId());
        }
        users.put(user.getId(), user);
        return new User(user);
    }

    @Override
    public void deleteUserById(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
    }

    private Long generateId() {
        return ++id;
    }
}
