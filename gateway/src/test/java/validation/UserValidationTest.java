package validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserValidationTest {

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "    "})
    public void createUserWithInvalidName(String name) {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name(name)
                .email("email")
                .build();

        Assertions.assertTrue(ValidationTestUtils.dtoHasErrorMessage(userDto, "Имя пользователя не может быть пустым."));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "test.ru", "   .com", "@test", "@.org", "test@"})
    @DisplayName("Проверка невозможности добавить пользователя с неправильно заданным email")
    public void createUserWithInvalidEmail(String email) {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email(email)
                .build();

        Assertions.assertTrue(ValidationTestUtils.dtoHasErrorMessage(userDto, "Некорректный формат электронной почты."));
    }

    @Test
    @DisplayName("Проверка невозможности добавления пользователя, когда name = null")
    public void createUserWithNullName() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name(null)
                .email("email")
                .build();

        Assertions.assertTrue(ValidationTestUtils.dtoHasErrorMessage(userDto, "Имя пользователя не может быть пустым."));
    }

    @Test
    @DisplayName("Проверка невозможности добавить пользователя с email = null")
    public void createUserWithNullEmail() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email(null)
                .build();

        Assertions.assertTrue(ValidationTestUtils.dtoHasErrorMessage(userDto, "Должен быть обязательно указан email."));
    }

    @Test
    @DisplayName("Проверка возможности добавить пользователя со всеми валидными полями")
    public void createUserWithAllValidFilms() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("name")
                .email("email@test.ru")
                .build();
        assertTrue(ValidationTestUtils.VALIDATOR.validate(userDto).isEmpty());
    }

    @Test
    @DisplayName("Проверка невозможности добавить пользователя с несколькими невалидными полями")
    public void createUserWithSeveralInvalidFields() {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("")
                .email(null)
                .build();

        assertAll(
                () -> ValidationTestUtils.dtoHasErrorMessage(userDto, "Имя пользователя не может быть пустым."),
                () -> ValidationTestUtils.dtoHasErrorMessage(userDto, "Должен быть обязательно указан email.")
        );
    }
}
