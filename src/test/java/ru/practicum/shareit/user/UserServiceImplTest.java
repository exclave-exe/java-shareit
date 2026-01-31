package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    // ---------- getUser ----------
    @Test
    void shouldReturnUserWhenUserExists() {
        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(userWithId);
        when(userMapper.mapToResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.getUser(1L);

        assertEquals(responseDto, result);
        verify(userRepository).getUserById(any(Long.class));
        verify(userMapper).mapToResponseDto(any(User.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExists() {
        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getUser(1L));
        verify(userMapper, never()).mapToResponseDto(any(User.class));
    }

    // ---------- createUser ----------
    @Test
    void shouldCreateUserWhenEmailIsUnique() {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Test")
                .email("test@mail.com")
                .build();

        User userWithoutId = User.builder()
                .name("Test")
                .email("test@mail.com")
                .build();

        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userMapper.mapToUser(any(UserCreateDto.class))).thenReturn(userWithoutId);
        when(userRepository.saveUser(any(User.class))).thenReturn(userWithId);
        when(userMapper.mapToResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.createUser(createDto);

        assertEquals(responseDto, result);
        verify(userRepository).existsByEmail(any(String.class));
        verify(userMapper).mapToUser(any(UserCreateDto.class));
        verify(userRepository).saveUser(any(User.class));
        verify(userMapper).mapToResponseDto(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenEmailAlreadyExistsOnCreate() {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(createDto));
        verify(userMapper, never()).mapToUser(any(UserCreateDto.class));
        verify(userRepository, never()).saveUser(any(User.class));
        verify(userMapper, never()).mapToResponseDto(any(User.class));
    }

    // ---------- updateUser ----------
    @Test
    void shouldUpdateUserName() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("NewName")
                .email(null)
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("NewName")
                .email("test@mail.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("NewName")
                .email("test@mail.com")
                .build();

        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(userWithId);
        when(userRepository.saveUser(any(User.class))).thenReturn(updatedUser);
        when(userMapper.mapToResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(updateDto, 1L);

        assertEquals("NewName", result.getName());
        assertEquals("test@mail.com", result.getEmail());
        verify(userRepository).getUserById(any(Long.class));
        verify(userRepository).saveUser(any(User.class));
        verify(userMapper).mapToResponseDto(any(User.class));
        verify(userRepository, never()).existsByEmail(any(String.class));
    }

    @Test
    void shouldUpdateUserEmail() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name(null)
                .email("new@mail.com")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("Test")
                .email("new@mail.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("Test")
                .email("new@mail.com")
                .build();

        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(userWithId);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.saveUser(any(User.class))).thenReturn(updatedUser);
        when(userMapper.mapToResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(updateDto, 1L);

        assertEquals("Test", result.getName());
        assertEquals("new@mail.com", result.getEmail());
        verify(userRepository).getUserById(any(Long.class));
        verify(userRepository).existsByEmail(any(String.class));
        verify(userRepository).saveUser(any(User.class));
        verify(userMapper).mapToResponseDto(any(User.class));
    }

    @Test
    void shouldUpdateUserNameAndEmail() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("NewName")
                .email("new@mail.com")
                .build();

        User updatedUser = User.builder()
                .id(1L)
                .name("NewName")
                .email("new@mail.com")
                .build();

        UserResponseDto updatedResponse = UserResponseDto.builder()
                .id(1L)
                .name("NewName")
                .email("new@mail.com")
                .build();

        UserResponseDto responseDto = UserResponseDto.builder()
                .id(1L)
                .name("NewName")
                .email("new@mail.com")
                .build();

        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(userWithId);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.saveUser(any(User.class))).thenReturn(updatedUser);
        when(userMapper.mapToResponseDto(any(User.class))).thenReturn(responseDto);

        UserResponseDto result = userService.updateUser(updateDto, 1L);

        assertEquals("NewName", result.getName());
        assertEquals("new@mail.com", result.getEmail());
        verify(userRepository).getUserById(any(Long.class));
        verify(userRepository).existsByEmail(any(String.class));
        verify(userRepository).saveUser(any(User.class));
        verify(userMapper).mapToResponseDto(any(User.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentUser() {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("NewName")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.updateUser(updateDto, 999L));
        verify(userRepository).getUserById(any(Long.class));
        verify(userRepository, never()).existsByEmail(any(String.class));
        verify(userRepository, never()).saveUser(any(User.class));
        verify(userMapper, never()).mapToResponseDto(any(User.class));
    }

    @Test
    void shouldThrowConflictExceptionWhenUpdatingToExistingEmail() {
        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        UserUpdateDto updateDto = UserUpdateDto.builder()
                .email("existing@mail.com")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(userWithId);
        when(userRepository.existsByEmail(any(String.class))).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(updateDto, 1L));
        verify(userRepository).getUserById(any(Long.class));
        verify(userRepository).existsByEmail(any(String.class));
        verify(userRepository, never()).saveUser(any(User.class));
        verify(userMapper, never()).mapToResponseDto(any(User.class));
    }

    // ---------- deleteUser ----------
    @Test
    void shouldDeleteUserWhenExists() {
        User userWithId = User.builder()
                .id(1L)
                .name("Test")
                .email("test@mail.com")
                .build();

        when(userRepository.getUserById(any(Long.class))).thenReturn(userWithId);

        userService.deleteUser(1L);

        verify(userRepository).deleteUserById(any(Long.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDeletingNotExistingUser() {
        when(userRepository.getUserById(any(Long.class))).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteUserById(any(Long.class));
    }
}

