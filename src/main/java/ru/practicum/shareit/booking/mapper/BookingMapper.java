package ru.practicum.shareit.booking.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    ShortBookingDto toShortDto(Booking booking);

    List<BookingDto> toDtoList(List<Booking> bookings);

    @AfterMapping
    default void setBookerId(Booking booking, @MappingTarget ShortBookingDto shortBookingDto) {
        shortBookingDto.setBookerId(booking.getBooker().getId());
    }
}
