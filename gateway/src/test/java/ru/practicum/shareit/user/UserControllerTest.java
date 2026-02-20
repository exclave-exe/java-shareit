package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    @Test
    @SneakyThrows
    void getUser_whenUserIdIsValid_thenReturnOk() {
        when(userClient.getUser(1L)).thenReturn(ResponseEntity.ok(Map.of(
                "id", 1L,
                "name", "Ivan",
                "email", "ivan@example.com"
        )));

        mockMvc.perform(get("/users/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"));

        verify(userClient).getUser(1L);
    }

    @Test
    @SneakyThrows
    void getUser_whenUserIdNotValid_thenReturnBadRequest() {

        Assertions.assertAll(
                () -> mockMvc.perform(get("/users/{userId}", -1L))
                        .andExpect(status().isBadRequest()),
                () -> mockMvc.perform(get("/users/{userId}", 0L))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(userClient);
    }

    @Test
    @SneakyThrows
    void createUser_whenDtoIsValid_thenReturnCreated() {
        UserCreateDto valid = UserCreateDto.builder()
                .name("Ivan")
                .email("ivan@mail.com")
                .build();

        when(userClient.createUser(any()))
                .thenReturn(ResponseEntity.status(201).body(Map.of(
                        "id", 1,
                        "name", valid.getName(),
                        "email", valid.getEmail()
                )));


        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@mail.com"));

        verify(userClient).createUser(any());
    }

    @Test
    @SneakyThrows
    void createUser_whenDtoIsNotValid_thenReturnBadRequest() {

        UserCreateDto nullName = UserCreateDto.builder()
                .name(null)
                .email("test@mail.com")
                .build();

        UserCreateDto blankName = UserCreateDto.builder()
                .name("")
                .email("test@mail.com")
                .build();

        UserCreateDto nullEmail = UserCreateDto.builder()
                .name("Ivan")
                .email(null)
                .build();

        UserCreateDto blankEmail = UserCreateDto.builder()
                .name("Ivan")
                .email("")
                .build();

        UserCreateDto invalidEmail = UserCreateDto.builder()
                .name("Ivan")
                .email("wrong-email")
                .build();

        Assertions.assertAll(
                () -> mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(nullName)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(blankName)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(nullEmail)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(blankEmail)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidEmail)))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(userClient);
    }

    @Test
    @SneakyThrows
    void updateUser_whenDtoIsValid_thenReturnOk() {
        UserUpdateDto valid = UserUpdateDto.builder()
                .name("Ivan")
                .email("ivan@example.com")
                .build();

        when(userClient.updateUser(any(), any()))
                .thenReturn(ResponseEntity.status(200).body(Map.of(
                        "id", 1L,
                        "name", valid.getName(),
                        "email", valid.getEmail()
                )));

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(valid)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value(valid.getName()))
                .andExpect(jsonPath("$.email").value(valid.getEmail()));

        verify(userClient).updateUser(any(), any());
    }

    @Test
    @SneakyThrows
    void updateUser_whenDtoIsNotValid_thenReturnBadRequest() {
        UserUpdateDto invalidEmail = UserUpdateDto.builder()
                .name("Ivan")
                .email("wrong-email")
                .build();

        UserUpdateDto blankName = UserUpdateDto.builder()
                .name("")
                .email("ivan@example.com")
                .build();

        Assertions.assertAll(
                () -> mockMvc.perform(patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidEmail)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(patch("/users/{userId}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(blankName)))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(userClient);
    }

    @Test
    @SneakyThrows
    void deleteUser_whenUserIdIsNotValid_thenReturnBadRequest() {

        Assertions.assertAll(
                () -> mockMvc.perform(delete("/users/{userId}", -1L))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(delete("/users/{userId}", 0L))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(userClient);
    }

    @Test
    @SneakyThrows
    void deleteUser_whenUserIdIsValid_thenReturnNoContent() {

        when(userClient.deleteUser(1L))
                .thenReturn(ResponseEntity.noContent().build());

        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isNoContent());

        verify(userClient).deleteUser(1L);
    }
}