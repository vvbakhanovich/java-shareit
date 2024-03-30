package ru.practicum.shareit.booking.mapper;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingMapperTest {

    private BookingMapper bookingMapper;

    @BeforeAll
    void init() {
        bookingMapper = new BookingMapperImpl();
    }

    @Test
    @DisplayName("Проверка маппинга bookerId")
    void mapBookersIdToBookerId() {
        User booker = User.builder()
                .id(1L)
                .name("name")
                .email("email@email.com")
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        ShortBookingDto shortDto = bookingMapper.toShortDto(booking);

        assertThat(shortDto.getBookerId(), is(booker.getId()));
        assertThat(shortDto.getStatus(), is(booking.getStatus()));
        assertThat(shortDto.getStart(), is(booking.getStart()));
        assertThat(shortDto.getEnd(), is(booking.getEnd()));
        assertThat(shortDto.getId(), is(booking.getId()));
    }

}