package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.shareit.common.Constants.HEADER_USER_ID;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingServiceImpl bookingService;

    @InjectMocks
    private BookingController controller;

    private MockMvc mockMvc;

    private final ObjectMapper mapper = new ObjectMapper();
    private static final Long USER_ID = 1L;
    private static final Long BOOKING_ID = 1L;
    private static final Long ITEM_ID = 1L;
    private static final Long OWNER_ID = 2L;

    private User booker;
    private User owner;
    private Item item;
    private BookingResponseDto bookingResponseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper.registerModule(new JavaTimeModule());

        booker = new User();
        booker.setId(USER_ID);
        booker.setName("Booker");
        booker.setEmail("booker@example.com");

        owner = new User();
        owner.setId(OWNER_ID);
        owner.setName("Owner");
        owner.setEmail("owner@example.com");

        item = new Item();
        item.setId(ITEM_ID);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        bookingResponseDto = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.WAITING)
                .booker(booker)
                .item(item)
                .build();
    }

    @Test
    void getBooking_whenInvoked_thenReturnsBookingWithStatus200() throws Exception {
        when(bookingService.getBooking(USER_ID, BOOKING_ID)).thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", BOOKING_ID)
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(USER_ID))
                .andExpect(jsonPath("$.booker.name").value("Booker"))
                .andExpect(jsonPath("$.item.id").value(ITEM_ID))
                .andExpect(jsonPath("$.item.name").value("Test Item"));

        verify(bookingService).getBooking(USER_ID, BOOKING_ID);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getBookerBookings_whenInvokedWithDefaultState_thenReturnsBookingsListWithStatus200() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getBookerBookings(USER_ID, BookingState.ALL)).thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(USER_ID))
                .andExpect(jsonPath("$[0].item.id").value(ITEM_ID))
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService).getBookerBookings(USER_ID, BookingState.ALL);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getOwnerBookings_whenInvokedWithDefaultState_thenReturnsOwnerBookingsListWithStatus200() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);
        when(bookingService.getOwnerBookings(OWNER_ID, BookingState.ALL)).thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_USER_ID, OWNER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(BOOKING_ID))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[0].booker.id").value(USER_ID))
                .andExpect(jsonPath("$[0].item.id").value(ITEM_ID))
                .andExpect(jsonPath("$.length()").value(1));

        verify(bookingService).getOwnerBookings(OWNER_ID, BookingState.ALL);
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void createBooking_whenInvoked_thenReturnsCreatedBookingWithStatus200() throws Exception {

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(ITEM_ID)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingService.createBooking(eq(USER_ID), any(BookingCreateDto.class))).thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header(HEADER_USER_ID, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(USER_ID))
                .andExpect(jsonPath("$.item.id").value(ITEM_ID))
                .andExpect(jsonPath("$.item.name").value("Test Item"));

        verify(bookingService).createBooking(eq(USER_ID), any(BookingCreateDto.class));
        verifyNoMoreInteractions(bookingService);
    }

    @Test
    void approveBooking_whenInvoked_thenReturnsApprovedBookingWithStatus200() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(BOOKING_ID)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(BookingStatus.APPROVED)
                .booker(booker)
                .item(item)
                .build();

        when(bookingService.approveBooking(OWNER_ID, BOOKING_ID, true)).thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", BOOKING_ID)
                        .header(HEADER_USER_ID, OWNER_ID)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(BOOKING_ID))
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.booker.id").value(USER_ID))
                .andExpect(jsonPath("$.item.id").value(ITEM_ID));

        verify(bookingService).approveBooking(OWNER_ID, BOOKING_ID, true);
        verifyNoMoreInteractions(bookingService);
    }
}