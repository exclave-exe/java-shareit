package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingServiceImpl bookingServiceImpl;
    private final BookingMapper bookingMapper;

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                         @PathVariable @Positive Long bookingId) {
        return bookingServiceImpl.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookerBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                      @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingServiceImpl.getBookerBookings(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getOwnerBookings(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                                     @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingServiceImpl.getOwnerBookings(userId, state);
    }

    @PostMapping
    public BookingResponseDto createBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                            @RequestBody @Valid BookingCreateDto bookingCreateDto) {
        return bookingServiceImpl.createBooking(userId, bookingCreateDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto approveBooking(@RequestHeader("X-Sharer-User-Id") @Positive Long userId,
                                             @PathVariable @Positive Long bookingId,
                                             @RequestParam Boolean approved) {
        return bookingServiceImpl.approveBooking(userId, bookingId, approved);
    }
}
