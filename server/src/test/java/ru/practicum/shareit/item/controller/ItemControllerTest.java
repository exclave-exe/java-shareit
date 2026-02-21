package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemExtendedResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Constants.HEADER_USER_ID;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController controller;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getItem_whenInvoked_thenReturnsItemWithStatus200() throws Exception {
        ItemExtendedResponseDto itemDto = ItemExtendedResponseDto.builder()
                .id(ITEM_ID)
                .name("Test Item")
                .description("Test Item description")
                .available(true)
                .comments(List.of())
                .build();
        when(itemService.getItem(USER_ID, ITEM_ID)).thenReturn(itemDto);

        mockMvc.perform(get("/items/{itemId}", ITEM_ID)
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.name").value("Test Item"))
                .andExpect(jsonPath("$.description").value("Test Item description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments").isEmpty());

        verify(itemService).getItem(USER_ID, ITEM_ID);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void getUserItems_whenInvoked_thenReturnsItemsListWithStatus200() throws Exception {
        ItemExtendedResponseDto itemDto = ItemExtendedResponseDto.builder()
                .id(1L)
                .name("User Item")
                .description("User Item description")
                .available(true)
                .comments(List.of())
                .build();
        when(itemService.getUserItems(USER_ID)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("User Item"))
                .andExpect(jsonPath("$[0].description").value("User Item description"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(jsonPath("$[0].comments").isEmpty())
                .andExpect(jsonPath("$.length()").value(1));

        verify(itemService).getUserItems(USER_ID);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void createItem_whenInvoked_thenReturnsCreatedItemWithStatus201() throws Exception {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New item description")
                .available(true)
                .requestId(1L)
                .build();
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("New Item")
                .description("New item description")
                .available(true)
                .comments(List.of())
                .build();
        when(itemService.createItem(USER_ID, createDto)).thenReturn(responseDto);

        mockMvc.perform(post("/items")
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("New Item"))
                .andExpect(jsonPath("$.description").value("New item description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments").isEmpty());

        verify(itemService).createItem(USER_ID, createDto);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void createComment_whenInvoked_thenReturnsCreatedCommentWithStatus201() throws Exception {
        CommentCreateDto commentDto = CommentCreateDto.builder()
                .text("Great item!")
                .build();
        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("User")
                .build();
        when(itemService.createComment(USER_ID, ITEM_ID, commentDto)).thenReturn(responseDto);

        mockMvc.perform(post("/items/{itemId}/comment", ITEM_ID)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(commentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Great item!"))
                .andExpect(jsonPath("$.authorName").value("User"));

        verify(itemService).createComment(USER_ID, ITEM_ID, commentDto);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void updateItem_whenInvoked_thenReturnsUpdatedItemWithStatus200() throws Exception {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .description("Updated description")
                .available(true)
                .build();
        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(ITEM_ID)
                .name("Updated Name")
                .description("Updated description")
                .available(true)
                .comments(List.of())
                .build();
        when(itemService.updateItem(USER_ID, ITEM_ID, updateDto)).thenReturn(responseDto);

        mockMvc.perform(patch("/items/{itemId}", ITEM_ID)
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ITEM_ID))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.available").value(true))
                .andExpect(jsonPath("$.comments").isArray())
                .andExpect(jsonPath("$.comments").isEmpty());

        verify(itemService).updateItem(USER_ID, ITEM_ID, updateDto);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void searchItems_whenValidText_thenReturnsFoundItemsWithStatus200() throws Exception {
        String searchText = "drill";
        ItemResponseDto itemDto = ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Drill description")
                .available(true)
                .comments(List.of())
                .build();
        when(itemService.searchItems(USER_ID, searchText)).thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .header(HEADER_USER_ID, USER_ID)
                        .param("text", searchText))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Drill"))
                .andExpect(jsonPath("$[0].description").value("Drill description"))
                .andExpect(jsonPath("$[0].available").value(true))
                .andExpect(jsonPath("$[0].comments").isArray())
                .andExpect(jsonPath("$[0].comments").isEmpty());

        verify(itemService).searchItems(USER_ID, searchText);
        verifyNoMoreInteractions(itemService);
    }

    @Test
    void searchItems_whenEmptyText_thenReturnsEmptyListWithStatus200() throws Exception {
        when(itemService.searchItems(USER_ID, null)).thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(itemService).searchItems(USER_ID, null);
        verifyNoMoreInteractions(itemService);
    }
}
