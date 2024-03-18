package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;

import java.util.List;

public interface BookingService {
    BookingDto addBooking(Long userId, AddBookingDto bookingDto);

    BookingDto acknowledgeBooking(Long userId, Long bookingId, Boolean approved);

    List<BookingDto> getAllBookingsFromUser(Long userId, GetBookingState state, Long from, Integer size, boolean isOwner);

    BookingDto getBookingById(Long userId, Long bookingId);
}
