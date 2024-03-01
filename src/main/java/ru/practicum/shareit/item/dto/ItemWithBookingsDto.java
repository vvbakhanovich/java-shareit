package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.ShortBookingDto;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemWithBookingsDto {

    private long id;

    private String name;

    private String description;

    private Boolean available;

    private ShortBookingDto lastBooking;

    private ShortBookingDto nextBooking;
}
