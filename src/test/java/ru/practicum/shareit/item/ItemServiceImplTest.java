package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    // ---------- getItem ----------
    @Test
    void shouldReturnItemWhenItemExists() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(item);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.getItem(1L, 1L);

        assertEquals(responseDto, result);
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExistsOnGet() {
        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.getItem(1L, 1L));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).getItemById(any(Long.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenItemNotExists() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.getItem(1L, 1L));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    // ---------- createItem ----------
    @Test
    void shouldCreateItemWhenUserExists() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        Item itemWithoutId = Item.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        Item itemWithId = Item.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemMapper.mapToItem(any(ItemCreateDto.class), any(User.class))).thenReturn(itemWithoutId);
        when(itemRepository.saveItem(any(Item.class))).thenReturn(itemWithId);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.createItem(1L, createDto);

        assertEquals(responseDto, result);
        verify(userRepository).getUserById(any(Long.class));
        verify(itemMapper).mapToItem(any(ItemCreateDto.class), any(User.class));
        verify(itemRepository).saveItem(any(Item.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExistsOnCreate() {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("Item")
                .description("Description")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.createItem(1L, createDto));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemMapper, never()).mapToItem(any(ItemCreateDto.class), any(User.class));
        verify(itemRepository, never()).saveItem(any(Item.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    // ---------- updateItem ----------
    @Test
    void shouldUpdateItemName() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("OldName")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("NewName")
                .description(null)
                .available(null)
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("NewName")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("NewName")
                .description("Description")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(item);
        when(itemRepository.saveItem(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("NewName", result.getName());
        assertEquals("Description", result.getDescription());
        assertTrue(result.getAvailable());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository).saveItem(any(Item.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldUpdateItemDescription() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("OldDescription")
                .available(true)
                .owner(owner)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name(null)
                .description("NewDescription")
                .available(null)
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("NewDescription")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("ItemName")
                .description("NewDescription")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(item);
        when(itemRepository.saveItem(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("ItemName", result.getName());
        assertEquals("NewDescription", result.getDescription());
        assertTrue(result.getAvailable());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository).saveItem(any(Item.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldUpdateItemAvailability() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name(null)
                .description(null)
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("Description")
                .available(false)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("ItemName")
                .description("Description")
                .available(false)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(item);
        when(itemRepository.saveItem(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("ItemName", result.getName());
        assertEquals("Description", result.getDescription());
        assertEquals(false, result.getAvailable());
        assertFalse(result.getAvailable());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository).saveItem(any(Item.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldUpdateAllItemFields() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("OldName")
                .description("OldDescription")
                .available(true)
                .owner(owner)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .id(1L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .owner(owner)
                .build();

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("NewName")
                .description("NewDescription")
                .available(false)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(item);
        when(itemRepository.saveItem(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.mapToResponseDto(any(Item.class))).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertEquals("NewName", result.getName());
        assertEquals("NewDescription", result.getDescription());
        assertEquals(false, result.getAvailable());
        assertFalse(result.getAvailable());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository).saveItem(any(Item.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExistsOnUpdate() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("NewName")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 1L, updateDto));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).getItemById(any(Long.class));
        verify(itemRepository, never()).saveItem(any(Item.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenItemNotExistsOnUpdate() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("NewName")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getItemById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(1L, 1L, updateDto));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository, never()).saveItem(any(Item.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserIsNotOwner() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        User anotherUser = User.builder()
                .id(2L)
                .name("AnotherUser")
                .email("another@mail.com")
                .build();

        Item item = Item.builder()
                .id(1L)
                .name("ItemName")
                .description("Description")
                .available(true)
                .owner(owner)
                .build();

        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("NewName")
                .build();

        when(userRepository.getUserById(2L)).thenReturn(anotherUser);
        when(itemRepository.getItemById(1L)).thenReturn(item);

        assertThrows(NotFoundException.class, () -> itemService.updateItem(2L, 1L, updateDto));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getItemById(any(Long.class));
        verify(itemRepository, never()).saveItem(any(Item.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    // ---------- getUserItems ----------
    @Test
    void shouldReturnUserItemsWhenUserExists() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item1 = Item.builder()
                .id(1L)
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(owner)
                .build();

        Item item2 = Item.builder()
                .id(2L)
                .name("Item2")
                .description("Description2")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto1 = ItemResponseDto.builder()
                .id(1L)
                .name("Item1")
                .description("Description1")
                .available(true)
                .build();

        ItemResponseDto responseDto2 = ItemResponseDto.builder()
                .id(2L)
                .name("Item2")
                .description("Description2")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getUserItems(1L)).thenReturn(List.of(item1, item2));
        when(itemMapper.mapToResponseDto(item1)).thenReturn(responseDto1);
        when(itemMapper.mapToResponseDto(item2)).thenReturn(responseDto2);

        List<ItemResponseDto> result = itemService.getUserItems(1L);

        assertEquals(2, result.size());
        assertEquals(responseDto1, result.get(0));
        assertEquals(responseDto2, result.get(1));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getUserItems(any(Long.class));
        verify(itemMapper, times(2)).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoItems() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.getUserItems(1L)).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.getUserItems(1L);

        assertTrue(result.isEmpty());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).getUserItems(any(Long.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExistsOnGetUserItems() {
        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.getUserItems(1L));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).getUserItems(any(Long.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    // ---------- searchItems ----------
    @Test
    void shouldReturnItemsWhenSearchQueryMatches() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        Item item1 = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .owner(owner)
                .build();

        ItemResponseDto responseDto1 = ItemResponseDto.builder()
                .id(1L)
                .name("Drill")
                .description("Power drill")
                .available(true)
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.searchItems("drill")).thenReturn(List.of(item1));
        when(itemMapper.mapToResponseDto(item1)).thenReturn(responseDto1);

        List<ItemResponseDto> result = itemService.searchItems(1L, "drill");

        assertEquals(1, result.size());
        assertEquals(responseDto1, result.getFirst());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).searchItems(any(String.class));
        verify(itemMapper).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldReturnEmptyListWhenSearchQueryIsBlank() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);

        List<ItemResponseDto> result = itemService.searchItems(1L, "   ");

        assertTrue(result.isEmpty());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).searchItems(any(String.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldReturnEmptyListWhenSearchQueryIsNull() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);

        List<ItemResponseDto> result = itemService.searchItems(1L, null);

        assertTrue(result.isEmpty());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).searchItems(any(String.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldReturnEmptyListWhenNoItemsMatchSearch() {
        User owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@mail.com")
                .build();

        when(userRepository.getUserById(1L)).thenReturn(owner);
        when(itemRepository.searchItems("nonexistent")).thenReturn(Collections.emptyList());

        List<ItemResponseDto> result = itemService.searchItems(1L, "nonexistent");

        assertTrue(result.isEmpty());
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository).searchItems(any(String.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUserNotExistsOnSearch() {
        when(userRepository.getUserById(1L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> itemService.searchItems(1L, "drill"));
        verify(userRepository).getUserById(any(Long.class));
        verify(itemRepository, never()).searchItems(any(String.class));
        verify(itemMapper, never()).mapToResponseDto(any(Item.class));
    }
}
