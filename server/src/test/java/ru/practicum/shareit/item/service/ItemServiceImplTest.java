package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingShortResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentCreateDto;
import ru.practicum.shareit.comment.dto.CommentResponseDto;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemExtendedResponseDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemRequest itemRequest;
    private LocalDateTime now;
    private Comment comment;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@test.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@test.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .owner(owner)
                .build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Test Request")
                .requestor(booker)
                .created(now)
                .build();

        comment = Comment.builder()
                .id(1L)
                .text("Great item!")
                .item(item)
                .author(booker)
                .created(now)
                .build();

        lastBooking = Booking.builder()
                .id(1L)
                .startBooking(now.minusDays(5))
                .endBooking(now.minusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();

        nextBooking = Booking.builder()
                .id(2L)
                .startBooking(now.plusDays(2))
                .endBooking(now.plusDays(5))
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void getItem_WhenUserIsOwner_ShouldReturnItemWithBookings() {

        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        ItemExtendedResponseDto extendedDto = ItemExtendedResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        when(itemMapper.mapToExtendedResponseDto(item)).thenReturn(extendedDto);

        BookingShortResponseDto lastBookingDto = BookingShortResponseDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();
        BookingShortResponseDto nextBookingDto = BookingShortResponseDto.builder()
                .id(2L)
                .bookerId(2L)
                .build();

        when(bookingRepository.findLastBookingsForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingsForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(bookingMapper.mapToShortDto(lastBooking)).thenReturn(lastBookingDto);
        when(bookingMapper.mapToShortDto(nextBooking)).thenReturn(nextBookingDto);


        ItemExtendedResponseDto result = itemService.getItem(1L, 1L);


        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Item", result.getName());
        verify(itemRepository).findByIdWithDetails(1L);
        verify(bookingRepository).findLastBookingsForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepository).findNextBookingsForItems(anyList(), any(LocalDateTime.class));
    }

    @Test
    void getItem_WhenUserIsNotOwner_ShouldReturnItemWithoutBookings() {

        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        ItemExtendedResponseDto extendedDto = ItemExtendedResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        when(itemMapper.mapToExtendedResponseDto(item)).thenReturn(extendedDto);


        ItemExtendedResponseDto result = itemService.getItem(2L, 1L);


        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
        verify(itemRepository).findByIdWithDetails(1L);
        verify(bookingRepository, never()).findLastBookingsForItems(anyList(), any(LocalDateTime.class));
    }

    @Test
    void getItem_WhenItemNotFound_ShouldThrowNotFoundException() {
        when(itemRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItem(1L, 99L));
        assertEquals("Item with id=99 not found", exception.getMessage());
        verify(itemRepository).findByIdWithDetails(99L);
    }

    @Test
    void getUserItems_WhenUserHasItems_ShouldReturnItemsWithBookings() {
        List<Item> items = List.of(item);
        when(itemRepository.findByOwnerIdWithDetails(1L)).thenReturn(items);

        ItemExtendedResponseDto extendedDto = ItemExtendedResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        when(itemMapper.mapToExtendedResponseDto(item)).thenReturn(extendedDto);

        BookingShortResponseDto lastBookingDto = BookingShortResponseDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();
        BookingShortResponseDto nextBookingDto = BookingShortResponseDto.builder()
                .id(2L)
                .bookerId(2L)
                .build();

        when(bookingRepository.findLastBookingsForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(lastBooking));
        when(bookingRepository.findNextBookingsForItems(anyList(), any(LocalDateTime.class)))
                .thenReturn(List.of(nextBooking));
        when(bookingMapper.mapToShortDto(lastBooking)).thenReturn(lastBookingDto);
        when(bookingMapper.mapToShortDto(nextBooking)).thenReturn(nextBookingDto);


        List<ItemExtendedResponseDto> result = itemService.getUserItems(1L);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        verify(itemRepository).findByOwnerIdWithDetails(1L);
        verify(bookingRepository).findLastBookingsForItems(anyList(), any(LocalDateTime.class));
        verify(bookingRepository).findNextBookingsForItems(anyList(), any(LocalDateTime.class));
    }

    @Test
    void getUserItems_WhenUserHasNoItems_ShouldReturnEmptyList() {
        when(itemRepository.findByOwnerIdWithDetails(1L)).thenReturn(Collections.emptyList());

        List<ItemExtendedResponseDto> result = itemService.getUserItems(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository).findByOwnerIdWithDetails(1L);
        verify(bookingRepository, never()).findLastBookingsForItems(anyList(), any(LocalDateTime.class));
    }

    @Test
    void createItem_WithoutRequest_ShouldCreateItem() {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .requestId(null)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.mapToItem(createDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.createItem(1L, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("New Item", result.getName());
        verify(userRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
        verify(itemRequestRepository, never()).findById(anyLong());
    }

    @Test
    void createItem_WithRequest_ShouldCreateItem() {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .requestId(1L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemMapper.mapToItem(createDto)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("New Item")
                .description("New Description")
                .available(true)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.createItem(1L, createDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository).findById(1L);
        verify(itemRequestRepository).findById(1L);
        verify(itemRepository).save(any(Item.class));
        assertEquals(itemRequest, item.getRequest());
    }

    @Test
    void createItem_WhenUserNotFound_ShouldThrowNotFoundException() {
        ItemCreateDto createDto = ItemCreateDto.builder().build();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(99L, createDto));
        assertEquals("User not found with id: 99", exception.getMessage());
        verify(userRepository).findById(99L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createItem_WhenRequestNotFound_ShouldThrowNotFoundException() {
        ItemCreateDto createDto = ItemCreateDto.builder()
                .name("New Item")
                .description("New Description")
                .available(true)
                .requestId(99L)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.mapToItem(createDto)).thenReturn(item);
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createItem(1L, createDto));
        assertEquals("Request not found with id: 99", exception.getMessage());

        verify(userRepository).findById(1L);
        verify(itemMapper).mapToItem(createDto);
        verify(itemRequestRepository).findById(99L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createComment_ShouldCreateComment() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();
        commentCreateDto.setText("Great item!");

        when(userRepository.existsById(2L)).thenReturn(true);
        when(itemRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBookingBefore(
                eq(2L), eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(true);

        when(commentMapper.mapToComment(commentCreateDto)).thenReturn(comment);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentResponseDto responseDto = CommentResponseDto.builder()
                .id(1L)
                .text("Great item!")
                .authorName("Booker")
                .created(now)
                .build();
        when(commentMapper.mapToResponseDto(comment)).thenReturn(responseDto);

        CommentResponseDto result = itemService.createComment(2L, 1L, commentCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Great item!", result.getText());
        assertEquals("Booker", result.getAuthorName());
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_WhenUserNotFound_ShouldThrowNotFoundException() {

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        when(userRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(99L, 1L, commentCreateDto));
        assertEquals("User with id=99 not found", exception.getMessage());
    }

    @Test
    void createComment_WhenItemNotFound_ShouldThrowNotFoundException() {

        CommentCreateDto commentCreateDto = new CommentCreateDto();
        when(userRepository.existsById(2L)).thenReturn(true);
        when(itemRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.createComment(2L, 99L, commentCreateDto));
        assertEquals("User with id=99 not found", exception.getMessage());
    }

    @Test
    void createComment_WhenNoCompletedBooking_ShouldThrowBadRequestException() {
        CommentCreateDto commentCreateDto = new CommentCreateDto();

        when(userRepository.existsById(2L)).thenReturn(true);
        when(itemRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBookingBefore(
                eq(2L), eq(1L), eq(BookingStatus.APPROVED), any(LocalDateTime.class)))
                .thenReturn(false);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> itemService.createComment(2L, 1L, commentCreateDto));
        assertEquals("User has no completed booking for this item", exception.getMessage());
    }

    @Test
    void updateItem_ShouldUpdateAllFields() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToItem(updateDto)).thenReturn(updatedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Updated Description")
                .available(false)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Updated Name", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(false, result.getAvailable());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_ShouldUpdateOnlyName() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .name("Updated Name")
                .build();

        Item updatedItem = Item.builder()
                .name("Updated Name")
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToItem(updateDto)).thenReturn(updatedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Updated Name")
                .description("Test Description")
                .available(true)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated Name", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(true, result.getAvailable());
    }

    @Test
    void updateItem_ShouldUpdateOnlyDescription() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .description("Updated Description")
                .build();

        Item updatedItem = Item.builder()
                .description("Updated Description")
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToItem(updateDto)).thenReturn(updatedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Updated Description")
                .available(true)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(true, result.getAvailable());
    }

    @Test
    void updateItem_ShouldUpdateOnlyAvailable() {
        ItemUpdateDto updateDto = ItemUpdateDto.builder()
                .available(false)
                .build();

        Item updatedItem = Item.builder()
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.mapToItem(updateDto)).thenReturn(updatedItem);
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(false)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);

        ItemResponseDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Test Item", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals(false, result.getAvailable());
    }

    @Test
    void updateItem_WhenUserIsNotOwner_ShouldThrowNotFoundException() {

        ItemUpdateDto updateDto = ItemUpdateDto.builder().build();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(2L, 1L, updateDto));
        assertEquals("Item with id=1 not found", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void updateItem_WhenItemNotFound_ShouldThrowNotFoundException() {

        ItemUpdateDto updateDto = ItemUpdateDto.builder().build();
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(1L, 99L, updateDto));
        assertEquals("Item with id=99 not found", exception.getMessage());
    }

    @Test
    void searchItems_WithValidQuery_ShouldReturnResults() {

        String searchText = "test";
        List<Item> items = List.of(item);

        when(itemRepository.searchAvailableItemsByText(searchText)).thenReturn(items);

        ItemResponseDto responseDto = ItemResponseDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .build();
        when(itemMapper.mapToResponseDto(item)).thenReturn(responseDto);


        List<ItemResponseDto> result = itemService.searchItems(1L, searchText);


        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Item", result.get(0).getName());
        verify(itemRepository).searchAvailableItemsByText(searchText);
    }

    @Test
    void searchItems_WithEmptyQuery_ShouldReturnEmptyList() {

        List<ItemResponseDto> result = itemService.searchItems(1L, "");


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItemsByText(anyString());
    }

    @Test
    void searchItems_WithNullQuery_ShouldReturnEmptyList() {

        List<ItemResponseDto> result = itemService.searchItems(1L, null);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItemsByText(anyString());
    }

    @Test
    void searchItems_WithBlankQuery_ShouldReturnEmptyList() {

        List<ItemResponseDto> result = itemService.searchItems(1L, "   ");


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItemsByText(anyString());
    }

    @Test
    void searchItems_WhenNoItemsFound_ShouldReturnEmptyList() {

        String searchText = "nonexistent";
        when(itemRepository.searchAvailableItemsByText(searchText)).thenReturn(Collections.emptyList());


        List<ItemResponseDto> result = itemService.searchItems(1L, searchText);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository).searchAvailableItemsByText(searchText);
    }
}