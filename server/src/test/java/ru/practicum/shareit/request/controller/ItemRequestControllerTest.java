package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.item.dto.ItemShortResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestExtendedResponseDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Constants.HEADER_USER_ID;

@ExtendWith(MockitoExtension.class)
class ItemRequestControllerTest {

    @Mock
    private ItemRequestService itemRequestService;

    @InjectMocks
    private ItemRequestController controller;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Long USER_ID = 1L;
    private static final Long REQUEST_ID = 1L;
    private static final LocalDateTime CREATED_DATE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createItemRequest_whenInvoked_thenReturnsCreatedRequestWithStatus200() throws Exception {
        ItemRequestCreateDto createDto = new ItemRequestCreateDto();
        createDto.setDescription("Need a drill");

        ItemRequestResponseDto responseDto = ItemRequestResponseDto.builder()
                .id(REQUEST_ID)
                .description("Need a drill")
                .created(CREATED_DATE)
                .build();

        when(itemRequestService.createItemRequest(USER_ID, createDto)).thenReturn(responseDto);

        mockMvc.perform(post("/requests")
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists());

        verify(itemRequestService).createItemRequest(USER_ID, createDto);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllItemRequests_whenInvoked_thenReturnsRequestsListWithStatus200() throws Exception {
        ItemRequestResponseDto requestDto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Need a drill")
                .created(CREATED_DATE)
                .build();
        ItemRequestResponseDto requestDto2 = ItemRequestResponseDto.builder()
                .id(2L)
                .description("Need a ladder")
                .created(CREATED_DATE)
                .build();

        when(itemRequestService.getAllItemRequest(USER_ID)).thenReturn(List.of(requestDto, requestDto2));

        mockMvc.perform(get("/requests/all")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].description").value("Need a ladder"))
                .andExpect(jsonPath("$[1].created").exists())
                .andExpect(jsonPath("$.length()").value(2));

        verify(itemRequestService).getAllItemRequest(USER_ID);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllRequestorItemRequests_whenInvoked_thenReturnsExtendedRequestsListWithStatus200() throws Exception {
        ItemShortResponseDto itemDto = ItemShortResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .build();

        ItemRequestExtendedResponseDto requestDto = ItemRequestExtendedResponseDto.builder()
                .id(REQUEST_ID)
                .description("Need a drill")
                .created(CREATED_DATE)
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getAllRequestorItemRequests(USER_ID)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/requests")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(REQUEST_ID))
                .andExpect(jsonPath("$[0].description").value("Need a drill"))
                .andExpect(jsonPath("$[0].created").exists())
                .andExpect(jsonPath("$[0].items[0].id").value(1L))
                .andExpect(jsonPath("$[0].items[0].name").value("Drill"))
                .andExpect(jsonPath("$[0].items[0].description").value("Powerful drill"));

        verify(itemRequestService).getAllRequestorItemRequests(USER_ID);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllRequestorItemRequests_whenNoRequests_thenReturnsEmptyListWithStatus200() throws Exception {
        when(itemRequestService.getAllRequestorItemRequests(USER_ID)).thenReturn(List.of());

        mockMvc.perform(get("/requests")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemRequestService).getAllRequestorItemRequests(USER_ID);
        verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getItemRequestById_whenInvoked_thenReturnsExtendedRequestWithStatus200() throws Exception {
        ItemShortResponseDto itemDto = ItemShortResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .build();

        ItemRequestExtendedResponseDto responseDto = ItemRequestExtendedResponseDto.builder()
                .id(REQUEST_ID)
                .description("Need a drill")
                .created(CREATED_DATE)
                .items(List.of(itemDto))
                .build();

        when(itemRequestService.getRequestById(REQUEST_ID, USER_ID)).thenReturn(responseDto);

        mockMvc.perform(get("/requests/{requestId}", REQUEST_ID)
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(REQUEST_ID))
                .andExpect(jsonPath("$.description").value("Need a drill"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items[0].id").value(1L))
                .andExpect(jsonPath("$.items[0].name").value("Drill"))
                .andExpect(jsonPath("$.items[0].description").value("Powerful drill"));

        verify(itemRequestService).getRequestById(REQUEST_ID, USER_ID);
        verifyNoMoreInteractions(itemRequestService);
    }
}