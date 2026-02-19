package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
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
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserCreateDto userCreateDto;
    private UserUpdateDto userUpdateDto;
    private UserResponseDto userResponseDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        userCreateDto = UserCreateDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();

        userUpdateDto = UserUpdateDto.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void getUser_WhenUserExists_ShouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.mapToResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUser_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUser(99L));
        assertEquals("User with id=99 not found", exception.getMessage());
        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_WithUniqueEmail_ShouldCreateUser() {
        when(userMapper.mapToUser(userCreateDto)).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.createUser(userCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void createUser_WithExistingEmail_ShouldThrowConflictException() {
        when(userMapper.mapToUser(userCreateDto)).thenReturn(user);
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(userCreateDto));
        assertEquals("User with email=john@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail(user.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserExists_ShouldUpdateAllFields() {
        User updatedUser = User.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        UserResponseDto updatedResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("Jane Doe")
                .email("jane@example.com")
                .build();

        when(userMapper.mapToUser(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.mapToResponseDto(savedUser)).thenReturn(updatedResponseDto);

        UserResponseDto result = userService.updateUser(userUpdateDto, 1L);

        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        assertEquals("jane@example.com", result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("jane@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userMapper.mapToUser(userUpdateDto)).thenReturn(user);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(userUpdateDto, 99L));
        assertEquals("User with id=99 not found", exception.getMessage());
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_WhenEmailExists_ShouldThrowConflictException() {
        User updatedUser = User.builder()
                .email("existing@example.com")
                .build();

        when(userMapper.mapToUser(userUpdateDto)).thenReturn(updatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(userUpdateDto, 1L));
        assertEquals("User with email=existing@example.com already exists", exception.getMessage());
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ShouldUpdateOnlyName() {
        UserUpdateDto updateNameDto = UserUpdateDto.builder()
                .name("New Name")
                .build();

        User updatedUser = User.builder()
                .name("New Name")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("New Name")
                .email("john@example.com")
                .build();

        UserResponseDto updatedResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("New Name")
                .email("john@example.com")
                .build();

        when(userMapper.mapToUser(updateNameDto)).thenReturn(updatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.mapToResponseDto(savedUser)).thenReturn(updatedResponseDto);

        UserResponseDto result = userService.updateUser(updateNameDto, 1L);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_ShouldUpdateOnlyEmail() {
        UserUpdateDto updateEmailDto = UserUpdateDto.builder()
                .email("newemail@example.com")
                .build();

        User updatedUser = User.builder()
                .email("newemail@example.com")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("newemail@example.com")
                .build();

        UserResponseDto updatedResponseDto = UserResponseDto.builder()
                .id(1L)
                .name("John Doe")
                .email("newemail@example.com")
                .build();

        when(userMapper.mapToUser(updateEmailDto)).thenReturn(updatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.mapToResponseDto(savedUser)).thenReturn(updatedResponseDto);

        UserResponseDto result = userService.updateUser(updateEmailDto, 1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("newemail@example.com", result.getEmail());
        verify(userRepository).existsByEmail("newemail@example.com");
    }

    @Test
    void updateUser_WhenNoChanges_ShouldNotUpdate() {
        UserUpdateDto emptyUpdateDto = UserUpdateDto.builder().build();
        User emptyUser = User.builder().build();

        when(userMapper.mapToUser(emptyUpdateDto)).thenReturn(emptyUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(emptyUpdateDto, 1L);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void updateUser_WhenEmailSameAsExisting_ShouldNotValidate() {
        UserUpdateDto updateWithSameEmailDto = UserUpdateDto.builder()
                .email("john@example.com")
                .build();

        User updatedUser = User.builder()
                .email("john@example.com")
                .build();

        when(userMapper.mapToUser(updateWithSameEmailDto)).thenReturn(updatedUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.mapToResponseDto(user)).thenReturn(userResponseDto);

        UserResponseDto result = userService.updateUser(updateWithSameEmailDto, 1L);

        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void deleteUser_WhenUserExists_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.deleteUser(99L));
        assertEquals("User with id=99 not found", exception.getMessage());
        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}