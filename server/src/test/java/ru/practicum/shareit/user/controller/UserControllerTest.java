package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getUser_whenInvoked_thenReturnsUserWithStatus200() throws Exception {
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(USER_ID)
                .name("John Doe")
                .email("john.doe@example.com")
                .build();
        when(userService.getUser(USER_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/users/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        verify(userService).getUser(USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void createUser_whenInvoked_thenReturnsCreatedUserWithStatus201() throws Exception {
        UserCreateDto createDto = UserCreateDto.builder()
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .build();
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(2L)
                .name("Jane Smith")
                .email("jane.smith@example.com")
                .build();
        when(userService.createUser(createDto)).thenReturn(responseDto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane.smith@example.com"));

        verify(userService).createUser(createDto);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void updateUser_whenInvoked_thenReturnsUpdatedUserWithStatus200() throws Exception {
        UserUpdateDto updateDto = UserUpdateDto.builder()
                .name("Updated Name")
                .email("updated.email@example.com")
                .build();
        UserResponseDto responseDto = UserResponseDto.builder()
                .id(USER_ID)
                .name("Updated Name")
                .email("updated.email@example.com")
                .build();
        when(userService.updateUser(updateDto, USER_ID)).thenReturn(responseDto);

        mockMvc.perform(patch("/users/{userId}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated.email@example.com"));

        verify(userService).updateUser(updateDto, USER_ID);
        verifyNoMoreInteractions(userService);
    }

    @Test
    void deleteUser_whenInvoked_thenReturnsStatus204() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/users/{userId}", USER_ID))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(USER_ID);
        verifyNoMoreInteractions(userService);
    }
}