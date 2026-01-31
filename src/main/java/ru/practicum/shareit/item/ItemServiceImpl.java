package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserRepository userRepository;

    @Override
    public ItemResponseDto getItem(Long userId, Long itemId) {
        log.info("User with id={} getting item by id={}", userId, itemId);
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        return itemMapper.mapToResponseDto(item);
    }

    @Override
    public ItemResponseDto createItem(Long userId, ItemCreateDto userCreateDto) {
        log.info("User with id={} creating item", userId);
        User user = getUserOrThrow(userId);
        Item itemToCreate = itemMapper.mapToItem(userCreateDto, user);
        return itemMapper.mapToResponseDto(itemRepository.saveItem(itemToCreate));
    }

    @Override
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemUpdateDto userUpdateDto) {
        log.info("User with id={} updating item with id={}", userId, itemId);
        getUserOrThrow(userId);
        Item itemToUpdate = getItemOrThrow(itemId);

        if (!itemToUpdate.getOwner().getId().equals(userId)) {
            log.warn("User with id={} trying to update item with id={} owned by another user", userId, itemId);
            throw new NotFoundException("Item with id=" + itemId + " not found");
        }

        if (userUpdateDto.getName() != null) {
            log.debug("Updating item name to {}", userUpdateDto.getName());
            itemToUpdate.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getDescription() != null) {
            log.debug("Updating item description to {}", userUpdateDto.getDescription());
            itemToUpdate.setDescription(userUpdateDto.getDescription());
        }
        if (userUpdateDto.getAvailable() != null) {
            log.debug("Updating item availability to {}", userUpdateDto.getAvailable());
            itemToUpdate.setAvailable(userUpdateDto.getAvailable());
        }
        return itemMapper.mapToResponseDto(itemRepository.saveItem(itemToUpdate));
    }

    @Override
    public List<ItemResponseDto> getUserItems(Long userId) {
        log.info("User with id={} getting his items", userId);
        getUserOrThrow(userId);
        return itemRepository.getUserItems(userId).stream()
                .map(itemMapper::mapToResponseDto)
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItems(Long userId, String searchQuery) {
        log.info("User with id={} searching for items with query={}", userId, searchQuery);
        getUserOrThrow(userId);
        if (searchQuery == null || searchQuery.isBlank()) {
            log.warn("Empty search query");
            return Collections.emptyList();
        }
        return itemRepository.searchItems(searchQuery).stream()
                .map(itemMapper::mapToResponseDto)
                .toList();
    }

    private Item getItemOrThrow(Long id) {
        Item item = itemRepository.getItemById(id);
        if (item == null) {
            log.warn("Item with id={} not found", id);
            throw new NotFoundException("Item with id=" + id + " not found");
        }
        return item;
    }

    private User getUserOrThrow(Long id) {
        User user = userRepository.getUserById(id);
        if (user == null) {
            log.warn("User with id={} not found", id);
            throw new NotFoundException("User with id=" + id + " not found");
        }
        return user;
    }
}
