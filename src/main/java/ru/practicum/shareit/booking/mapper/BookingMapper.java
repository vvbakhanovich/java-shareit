package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    List<BookingDto> toDtoList(List<Booking> bookings);
}
