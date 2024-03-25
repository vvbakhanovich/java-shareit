package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final BookingClient bookingClient;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody AddBookingDto bookingDto) {
        log.info("Creating booking {}, userId={}", bookingDto, userId);
        return bookingClient.bookItem(userId, bookingDto);
    }


    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> acknowledgeBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @PathVariable Long bookingId,
                                                     @RequestParam Boolean approved) {
        return bookingClient.acknowledgeBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllBookingsFromUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                         @RequestParam(defaultValue = "ALL") GetBookingState state,
                                                         @PositiveOrZero @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                         @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("Get booking with state {}, userId={}, from={}, size={}", state, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(defaultValue = "ALL") GetBookingState state,
                                                      @PositiveOrZero @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        log.info("Get booking with state {}, userId={}, from={}, size={}", state, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }
}
