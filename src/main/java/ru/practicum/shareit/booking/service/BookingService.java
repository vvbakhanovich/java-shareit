package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(long userId, AddBookingDto bookingDto);

    BookingDto acknowledgeBooking(long userId, Long bookingId, Boolean approved);

    List<BookingDto> getAllBookingsFromUser(long userId, GetBookingState state);

    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getAllOwnerBookings(Long userId, GetBookingState state);
}
