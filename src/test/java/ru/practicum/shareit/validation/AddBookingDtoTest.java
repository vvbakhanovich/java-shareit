package ru.practicum.shareit.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.AddBookingDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static ru.practicum.shareit.validation.ValidationTestUtils.*;

class AddBookingDtoTest {

    @Test
    @DisplayName("Проверка невозможности создать бронирование без идентификатора вещи.")
    void testCreateWithoutItemId() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(null)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Не указан идентификатор вещи."));
    }

    @Test
    @DisplayName("Проверка невозможности создать бронирование без даты начала бронирования.")
    void testCreateWithoutStart() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(1L)
                .start(null)
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Не указана дата начала бронирования."));
    }

    @Test
    @DisplayName("Проверка невозможности создать бронирование без даты окончания бронирования.")
    void testCreateWithoutEnd() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(null)
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Не указана дата окончания бронирования."));
    }

    @Test
    @DisplayName("Проверка невозможности создать бронирование, когда дата начала в прошлом.")
    void testCreateWhenStartInPast() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Задан некорректный интервал бронирования."));
    }

    @Test
    @DisplayName("Проверка невозможности создать бронирование, когда дата начала сегодня.")
    void testCreateWhenStartNow() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusDays(5))
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Задан некорректный интервал бронирования."));
    }

    @Test
    @DisplayName("Проверка невозможности создать бронирование, когда дата окончания раньше даты начала.")
    void testCreateWhenEndEarlierThanStart() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(1))
                .build();

        assertTrue(dtoHasErrorMessage(addBookingDto, "Задан некорректный интервал бронирования."));
    }
}