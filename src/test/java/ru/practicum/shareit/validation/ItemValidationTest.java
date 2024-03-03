package ru.practicum.shareit.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.shareit.validation.ValidationTestUtils.VALIDATOR;
import static ru.practicum.shareit.validation.ValidationTestUtils.dtoHasErrorMessage;

public class ItemValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    @DisplayName("Проверка невозможности добавить вещь с пустым названием.")
    public void createItemWithEmptyName(String name) {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name(name)
                .description("description")
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemDto, "Название вещи не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности добавить вещь, если имя = null")
    public void createItemWithNullName() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name(null)
                .description("description")
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemDto, "Название вещи не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка возможности добавить вещь с валидным названием")
    public void createItemWithValidName() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .build();

        assertTrue(VALIDATOR.validate(itemDto).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    @DisplayName("Проверка невозможности добавить вещь с пустым описанием.")
    public void createItemWithEmptyDescription(String description) {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description(description)
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemDto, "Описание вещи не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности добавить вещь, если описание = null")
    public void createItemWithNullDescription() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description(null)
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemDto, "Описание вещи не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности добавить вещь, когда статус доступности = null")
    public void createItemWithNullAvailability() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .build();

        assertTrue(dtoHasErrorMessage(itemDto, "У вещи обязательно должен быть указан статус доступности."));
    }

    @Test
    @DisplayName("Проверка невозможности добавить вещь с несколькими невалидным полями.")
    public void createItemWithSeveralInvalidFields() {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("")
                .description(null)
                .build();

        assertAll(
                () -> dtoHasErrorMessage(itemDto, "Название не может быть пустым."),
                () -> dtoHasErrorMessage(itemDto, "Описание вещи не может быть пустым."),
                () -> dtoHasErrorMessage(itemDto, "У вещи обязательно должен быть указан статус доступности.")
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    @DisplayName("Проверка невозможности создать ItemUpdateDto с пустым name")
    public void createItemUpdateDtoWithEmptyName(String name) {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name(name)
                .description("description")
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemUpdateDto, "Название не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности создать ItemUpdateDto с name = null")
    public void createItemUpdateDtoWithNullName() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name(null)
                .description("description")
                .available(true)
                .build();

        assertTrue(dtoHasErrorMessage(itemUpdateDto, "Название не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности создать ItemUpdateDto с доступностью = null")
    public void createItemUpdateDtoWithNullAvailability() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("name")
                .description("description")
                .build();

        assertTrue(dtoHasErrorMessage(itemUpdateDto, "Не указан статус доступности."));
    }
}
