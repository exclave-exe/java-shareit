package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto getItem(Long userId, Long itemId);

    ItemResponseDto createItem(Long userId, ItemCreateDto userCreateDto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto userUpdateDto);

    List<ItemResponseDto> getUserItems(Long userId);

    List<ItemResponseDto> searchItems(Long userId, String searchQuery);
}
