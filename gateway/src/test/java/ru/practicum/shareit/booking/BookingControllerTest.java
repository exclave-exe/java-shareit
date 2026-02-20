package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.shareit.common.Constants.HEADER_USER_ID;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    @SneakyThrows
    void getBooking_whenHeaderAndPathValid_thenReturnOk() {

        when(bookingClient.getBooking(1L, 1L))
                .thenReturn(ResponseEntity.ok(Map.of(
                        "id", 1L,
                        "status", "APPROVED"
                )));

        mockMvc.perform(get("/bookings/{bookingId}", 1L)
                        .header(HEADER_USER_ID, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @SneakyThrows
    void getBooking_whenHeaderOrPathInvalid_thenReturnBadRequest() {

        Assertions.assertAll(
                () -> mockMvc.perform(get("/bookings/{bookingId}", 1L)
                                .header(HEADER_USER_ID, -1L))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(get("/bookings/{bookingId}", -1L)
                                .header(HEADER_USER_ID, 1L))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(get("/bookings/{bookingId}", 0L)
                                .header(HEADER_USER_ID, 1L))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }

    @Test
    @SneakyThrows
    void getBookerBookings_whenHeaderValid_thenReturnOk() {

        when(bookingClient.getBookerBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1L))));

        mockMvc.perform(get("/bookings")
                        .header(HEADER_USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @SneakyThrows
    void getBookerBookings_whenHeaderInvalid_thenReturnBadRequest() {
        Assertions.assertAll(
                () -> mockMvc.perform(get("/bookings")
                                .header(HEADER_USER_ID, -1L))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(get("/bookings")
                                .header(HEADER_USER_ID, 0L))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }

    @Test
    @SneakyThrows
    void getOwnerBookings_whenHeaderValid_thenReturnOk() {

        when(bookingClient.getOwnerBookings(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 2L))));

        mockMvc.perform(get("/bookings/owner")
                        .header(HEADER_USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2L));
    }

    @Test
    @SneakyThrows
    void getOwnerBookings_whenHeaderInvalid_thenReturnBadRequest() {
        Assertions.assertAll(
                () -> mockMvc.perform(get("/bookings/owner")
                                .header(HEADER_USER_ID, -1L))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(get("/bookings/owner")
                                .header(HEADER_USER_ID, 0L))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }

    @Test
    @SneakyThrows
    void createBooking_whenDtoAndHeaderValid_thenReturnOk() {

        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        when(bookingClient.createBooking(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1L)));

        mockMvc.perform(post("/bookings")
                        .header(HEADER_USER_ID, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @SneakyThrows
    void createBooking_whenDtoInvalid_thenReturnBadRequest() {

        BookingCreateDto nullItem = BookingCreateDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingCreateDto startPast = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        BookingCreateDto endPast = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().minusDays(1))
                .build();

        BookingCreateDto allNull = BookingCreateDto.builder().build();

        Assertions.assertAll(
                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, 1L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(nullItem)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, 1L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(startPast)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, 1L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(endPast)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, 1L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(allNull)))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }

    @Test
    @SneakyThrows
    void createBooking_whenHeaderInvalid_thenReturnBadRequest() {
        BookingCreateDto dto = BookingCreateDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Assertions.assertAll(
                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, -1L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(post("/bookings")
                                .header(HEADER_USER_ID, 0L)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }

    @Test
    @SneakyThrows
    void approveBooking_whenHeaderAndPathValid_thenReturnOk() {

        when(bookingClient.approveBooking(1L, 1L, true))
                .thenReturn(ResponseEntity.ok("approved"));

        mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                        .header(HEADER_USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(content().string("approved"));
    }

    @Test
    @SneakyThrows
    void approveBooking_whenHeaderOrPathInvalid_thenReturnBadRequest() {
        Assertions.assertAll(
                () -> mockMvc.perform(patch("/bookings/{bookingId}", -1L)
                                .header(HEADER_USER_ID, 1L)
                                .param("approved", "true"))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(patch("/bookings/{bookingId}", 0L)
                                .header(HEADER_USER_ID, 1L)
                                .param("approved", "true"))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                                .header(HEADER_USER_ID, -1L)
                                .param("approved", "true"))
                        .andExpect(status().isBadRequest()),

                () -> mockMvc.perform(patch("/bookings/{bookingId}", 1L)
                                .header(HEADER_USER_ID, 0L)
                                .param("approved", "true"))
                        .andExpect(status().isBadRequest())
        );

        verifyNoInteractions(bookingClient);
    }
}
