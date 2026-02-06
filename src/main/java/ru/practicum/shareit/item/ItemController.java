package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemExtendedResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemExtendedResponseDto getItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                   @PathVariable @Positive Long itemId) {
        return itemService.getItem(userId, itemId);
    }

    @GetMapping()
    public List<ItemExtendedResponseDto> getUserItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId) {
        return itemService.getUserItems(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto createItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @RequestBody @Valid ItemCreateDto itemCreateDto) {
        return itemService.createItem(userId, itemCreateDto);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto createComment(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                            @PathVariable @Positive Long itemId,
                                            @RequestBody @Valid CommentCreateDto commentCreateDto) {
        return itemService.createComment(userId, itemId, commentCreateDto);
    }


    @PatchMapping("/{itemId}")
    public ItemResponseDto updateItem(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                      @PathVariable @Positive Long itemId,
                                      @RequestBody @Valid ItemUpdateDto itemUpdateDto) {
        return itemService.updateItem(userId, itemId, itemUpdateDto);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItems(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @RequestParam(required = false) String text) {
        return itemService.searchItems(userId, text);
    }
}
