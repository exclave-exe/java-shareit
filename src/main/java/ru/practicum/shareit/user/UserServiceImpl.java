package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto getUser(Long id) {
        log.info("Getting user by id={}", id);
        User user = getUserOrThrow(id);
        return userMapper.mapToResponseDto(user);
    }

    @Override
    public UserResponseDto createUser(UserCreateDto userCreateDto) {
        log.info("Creating user with name={} and email={}", userCreateDto.getName(), userCreateDto.getEmail());
        validateEmailUnique(userCreateDto.getEmail());
        User userToCreate = userMapper.mapToUser(userCreateDto);
        return userMapper.mapToResponseDto(userRepository.saveUser(userToCreate));
    }

    @Override
    public UserResponseDto updateUser(UserUpdateDto userUpdateDto, Long id) {
        log.info("Updating user by id={}", id);
        User userToUpdate = getUserOrThrow(id);
        if (userUpdateDto.getName() != null) {
            log.debug("Updating user name to {}", userUpdateDto.getName());
            userToUpdate.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null) {
            log.debug("Updating user email to {}", userUpdateDto.getEmail());
            validateEmailUnique(userUpdateDto.getEmail());
            userToUpdate.setEmail(userUpdateDto.getEmail());
        }
        return userMapper.mapToResponseDto(userRepository.saveUser(userToUpdate));
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user by id={}", id);
        User user = getUserOrThrow(id);
        userRepository.deleteUserById(user.getId());
    }

    private User getUserOrThrow(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            log.warn("User with id={} not found", id);
            throw new NotFoundException("User with id=" + id + " not found");
        }
        return user;
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            log.warn("User with email={} already exists", email);
            throw new ConflictException("User with email=" + email + " already exists");
        }
    }
}
