package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto addNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @Valid @RequestBody AddBookingDto bookingDto) {
        return bookingService.addBooking(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto acknowledgeBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PathVariable Long bookingId,
                                         @RequestParam Boolean approved) {
        return bookingService.acknowledgeBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                     @PathVariable Long bookingId) {
        return bookingService.getBookingById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> getAllBookingsFromUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                   @RequestParam(defaultValue = "ALL") GetBookingState state,
                                                   @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                   @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        return bookingService.getAllBookingsFromUser(userId, state, from, size, false);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "ALL") GetBookingState state,
                                                @RequestParam(defaultValue = "0") Long from,
                                                @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        return bookingService.getAllBookingsFromUser(userId, state, from, size, true);
    }
}
