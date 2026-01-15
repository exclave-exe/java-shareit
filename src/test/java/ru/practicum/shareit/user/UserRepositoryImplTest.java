package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryImplTest {

    private UserRepositoryImpl userRepository;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = new UserRepositoryImpl();
        resetStaticId();
    }

    // ---------- saveUser ----------
    @Test
    void shouldSaveUserAndGenerateId() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();

        User saved = userRepository.saveUser(user);

        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
        assertEquals("Test", saved.getName());
        assertEquals("test@mail.com", saved.getEmail());
    }

    @Test
    void shouldGenerateSequentialIds() {
        User user1 = User.builder()
                .name("User1")
                .email("user1@mail.com")
                .build();

        User user2 = User.builder()
                .name("User2")
                .email("user2@mail.com")
                .build();

        User saved1 = userRepository.saveUser(user1);
        User saved2 = userRepository.saveUser(user2);

        assertEquals(1L, saved1.getId());
        assertEquals(2L, saved2.getId());
    }

    @Test
    void shouldUpdateExistingUserWhenIdIsPresent() {
        User user = User.builder()
                .name("Original")
                .email("original@mail.com")
                .build();
        User saved = userRepository.saveUser(user);

        saved.setName("Updated");
        saved.setEmail("updated@mail.com");
        User updated = userRepository.saveUser(saved);

        assertEquals(saved.getId(), updated.getId());
        assertEquals("Updated", updated.getName());
        assertEquals("updated@mail.com", updated.getEmail());
    }

    @Test
    void shouldReturnCopyOfSavedUser() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();

        User saved = userRepository.saveUser(user);
        saved.setName("Modified");

        User retrieved = userRepository.getUserById(saved.getId());
        assertEquals("Test", retrieved.getName());
    }

    // ---------- getUserById ----------
    @Test
    void shouldReturnUserById() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();
        User saved = userRepository.saveUser(user);

        User found = userRepository.getUserById(saved.getId());

        assertNotNull(found);
        assertEquals(saved.getId(), found.getId());
        assertEquals("Test", found.getName());
        assertEquals("test@mail.com", found.getEmail());
    }

    @Test
    void shouldReturnNullWhenUserNotExists() {
        User found = userRepository.getUserById(999L);

        assertNull(found);
    }

    @Test
    void shouldReturnCopyOfUserOnGet() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();
        User saved = userRepository.saveUser(user);

        User found = userRepository.getUserById(saved.getId());
        found.setName("Modified");

        User foundAgain = userRepository.getUserById(saved.getId());
        assertEquals("Test", foundAgain.getName());
    }

    // ---------- deleteUserById ----------
    @Test
    void shouldDeleteExistingUser() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();
        User saved = userRepository.saveUser(user);

        userRepository.deleteUserById(saved.getId());
        User found = userRepository.getUserById(saved.getId());

        assertNull(found);
    }

    @Test
    void shouldDoNothingWhenDeletingNonExistentUser() {
        assertDoesNotThrow(() -> userRepository.deleteUserById(999L));
    }

    // ---------- existsByEmail ----------
    @Test
    void shouldReturnTrueWhenEmailExists() {
        User user = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();
        userRepository.saveUser(user);

        boolean exists = userRepository.existsByEmail("test@mail.com");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmailNotExists() {
        boolean exists = userRepository.existsByEmail("nonexistent@mail.com");

        assertFalse(exists);
    }

    @Test
    void shouldReturnTrueWhenMultipleUsersAndEmailExists() {
        User user1 = User.builder()
                .name("User1")
                .email("user1@mail.com")
                .build();

        User user2 = User.builder()
                .name("User2")
                .email("user2@mail.com")
                .build();

        userRepository.saveUser(user1);
        userRepository.saveUser(user2);

        assertTrue(userRepository.existsByEmail("user1@mail.com"));
        assertTrue(userRepository.existsByEmail("user2@mail.com"));
        assertFalse(userRepository.existsByEmail("user3@mail.com"));
    }

    private void resetStaticId() throws Exception {
        Field idField = UserRepositoryImpl.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(null, 0L);
    }
}
