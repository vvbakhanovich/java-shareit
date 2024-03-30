package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortBookingDto {

    private Long id;

    private Long bookerId;

    private BookingStatus status;

    private LocalDateTime start;

    private LocalDateTime end;
}
