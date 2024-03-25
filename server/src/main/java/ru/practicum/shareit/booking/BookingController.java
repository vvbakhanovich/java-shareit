package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingDto addNewBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                    @RequestBody AddBookingDto bookingDto) {
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
                                                   @RequestParam GetBookingState state,
                                                   @RequestParam Long from,
                                                   @RequestParam Integer size) {
        return bookingService.getAllBookingsFromUser(userId, state, from, size, false);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllOwnerBookings(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam GetBookingState state,
                                                @RequestParam Long from,
                                                @RequestParam Integer size) {
        return bookingService.getAllBookingsFromUser(userId, state, from, size, true);
    }
}
