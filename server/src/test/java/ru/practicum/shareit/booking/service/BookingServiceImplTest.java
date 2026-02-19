package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingCreateDto bookingCreateDto;
    private BookingResponseDto bookingResponseDto;
    private LocalDateTime now;

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

        booking = Booking.builder()
                .id(1L)
                .startBooking(now.plusDays(1))
                .endBooking(now.plusDays(3))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingCreateDto = BookingCreateDto.builder()
                .itemId(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(3))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    void getBooking_WhenUserIsOwner_ShouldReturnBooking() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBooking(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).findByIdWithDetails(1L);
    }

    @Test
    void getBooking_WhenUserIsBooker_ShouldReturnBooking() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.getBooking(2L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findByIdWithDetails(1L);
    }

    @Test
    void getBooking_WhenUserIsNotOwnerOrBooker_ShouldThrowConflictException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        ConflictException exception = assertThrows(ConflictException.class,
                () -> bookingService.getBooking(3L, 1L));
        assertEquals("User with id=3 is not owner of booking with id=1", exception.getMessage());
    }

    @Test
    void getBooking_WhenBookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBooking(1L, 99L));
        assertEquals("Booking with id=99 not found", exception.getMessage());
    }

    @Test
    void getBookerBookings_WhenUserExists_ShouldReturnAllBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findAllByBookerId(2L)).thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByBookerId(2L);
    }

    @Test
    void getBookerBookings_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookerBookings(99L, BookingState.ALL));
        assertEquals("User with id=99 not found", exception.getMessage());
    }

    @Test
    void getBookerBookings_WithCurrentState_ShouldReturnCurrentBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findCurrentByBookerId(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.CURRENT);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findCurrentByBookerId(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void getBookerBookings_WithPastState_ShouldReturnPastBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findPastByBookerId(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.PAST);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findPastByBookerId(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void getBookerBookings_WithFutureState_ShouldReturnFutureBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findFutureByBookerId(eq(2L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findFutureByBookerId(eq(2L), any(LocalDateTime.class));
    }

    @Test
    void getBookerBookings_WithWaitingState_ShouldReturnWaitingBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.WAITING))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(2L, BookingStatus.WAITING);
    }

    @Test
    void getBookerBookings_WithRejectedState_ShouldReturnRejectedBookings() {
        when(userRepository.existsById(2L)).thenReturn(true);
        when(bookingRepository.findByBookerIdAndStatus(2L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getBookerBookings(2L, BookingState.REJECTED);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByBookerIdAndStatus(2L, BookingStatus.REJECTED);
    }

    @Test
    void getOwnerBookings_WhenUserExists_ShouldReturnAllBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findAllByOwnerId(1L)).thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.ALL);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findAllByOwnerId(1L);
    }

    @Test
    void getOwnerBookings_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getOwnerBookings(99L, BookingState.ALL));
        assertEquals("User with id=99 not found", exception.getMessage());
    }

    @Test
    void getOwnerBookings_WithCurrentState_ShouldReturnCurrentBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findCurrentByOwnerId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.CURRENT);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findCurrentByOwnerId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithPastState_ShouldReturnPastBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findPastByOwnerId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.PAST);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findPastByOwnerId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithFutureState_ShouldReturnFutureBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findFutureByOwnerId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.FUTURE);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findFutureByOwnerId(eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getOwnerBookings_WithWaitingState_ShouldReturnWaitingBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByOwnerIdAndStatus(1L, BookingStatus.WAITING))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.WAITING);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByOwnerIdAndStatus(1L, BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_WithRejectedState_ShouldReturnRejectedBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByOwnerIdAndStatus(1L, BookingStatus.REJECTED))
                .thenReturn(List.of(booking));
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        List<BookingResponseDto> result = bookingService.getOwnerBookings(1L, BookingState.REJECTED);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(bookingRepository).findByOwnerIdAndStatus(1L, BookingStatus.REJECTED);
    }

    @Test
    void createBooking_ShouldCreateBooking() {
        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.createBooking(2L, bookingCreateDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBooking_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(booking);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(99L, bookingCreateDto));
        assertEquals("User with id=99 not found", exception.getMessage());
    }

    @Test
    void createBooking_WhenEndDateBeforeStartDate_ShouldThrowBadRequestException() {
        Booking invalidBooking = Booking.builder()
                .startBooking(now.plusDays(3))
                .endBooking(now.plusDays(1))
                .build();

        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(invalidBooking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, bookingCreateDto));
        assertEquals("End date must be after start date", exception.getMessage());
    }

    @Test
    void createBooking_WhenEndDateEqualsStartDate_ShouldThrowBadRequestException() {
        Booking invalidBooking = Booking.builder()
                .startBooking(now.plusDays(1))
                .endBooking(now.plusDays(1))
                .build();

        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(invalidBooking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, bookingCreateDto));
        assertEquals("End date must be after start date", exception.getMessage());
    }

    @Test
    void createBooking_WhenOwnerTriesToBookOwnItem_ShouldThrowNotFoundException() {
        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(booking);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(1L, bookingCreateDto));
        assertEquals("Owner cannot book his own item", exception.getMessage());
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowBadRequestException() {
        item.setAvailable(false);
        when(bookingMapper.mapToBooking(bookingCreateDto)).thenReturn(booking);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(item));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, bookingCreateDto));
        assertEquals("Item with id=1 is not available", exception.getMessage());
    }

    @Test
    void approveBooking_WhenApproved_ShouldSetStatusApproved() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_WhenRejected_ShouldSetStatusRejected() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.mapToResponseDto(booking)).thenReturn(bookingResponseDto);

        BookingResponseDto result = bookingService.approveBooking(1L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, booking.getStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void approveBooking_WhenBookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 99L, true));
        assertEquals("Booking with id=99 not found", exception.getMessage());
    }

    @Test
    void approveBooking_WhenUserIsNotOwner_ShouldThrowBadRequestException() {
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(2L, 1L, true));
        assertEquals("User with id=2 is not owner of booking with id=1", exception.getMessage());
    }

    @Test
    void approveBooking_WhenBookingNotInWaitingStatus_ShouldThrowBadRequestException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(booking));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        assertEquals("Booking status has already been changed", exception.getMessage());
    }
}