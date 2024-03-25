package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto userDto;

    private UserUpdateDto updateDto;

    @BeforeEach
    void init() {
        userDto = UserDto.builder()
                .name("username")
                .email("test@email.com")
                .build();
        updateDto = UserUpdateDto.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();
    }

    @Test
    @DisplayName("Добавление пользователя")
    void addUser_ShouldReturnUserWithId() {
        UserDto savedUser = userService.addUser(userDto);

        assertThat(savedUser.getId(), notNullValue());
        assertThat(savedUser.getId(), greaterThan(0L));
        assertThat(savedUser.getName(), is(userDto.getName()));
        assertThat(savedUser.getEmail(), is(userDto.getEmail()));
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    void updateUser_WithNameAndEmail_ShouldUpdateNameAndEmail() {
        UserDto savedUser = userService.addUser(userDto);
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(updateDto.getName()));
        assertThat(updatedUser.getEmail(), is(updateDto.getEmail()));
    }

    @Test
    @DisplayName("Обновление email пользователя")
    void updateUser_WithOnlyEmail_ShouldUpdateEmail() {
        updateDto.setName(null);
        UserDto savedUser = userService.addUser(userDto);
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(updateDto.getEmail()));
    }

    @Test
    @DisplayName("Обновление имени пользователя")
    void updateUser_WithOnlyName_ShouldUpdateName() {
        updateDto.setEmail(null);
        UserDto savedUser = userService.addUser(userDto);
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(updateDto.getName()));
        assertThat(updatedUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Обновление данных пользователя, email и name равны null")
    void updateUser_WithNullEmailAndName_ShouldNotUpdateAnyFields() {
        updateDto.setEmail(null);
        updateDto.setName(null);
        UserDto savedUser = userService.addUser(userDto);
        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertThat(updatedUser.getId(), is(savedUser.getId()));
        assertThat(updatedUser.getName(), is(savedUser.getName()));
        assertThat(updatedUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    void findUserById_UserFound_ShouldReturnUser() {
        UserDto savedUser = userService.addUser(userDto);

        UserDto foundUser = userService.findUserById(savedUser.getId());

        assertThat(foundUser.getId(), is(savedUser.getId()));
        assertThat(foundUser.getName(), is(savedUser.getName()));
        assertThat(foundUser.getEmail(), is(savedUser.getEmail()));
    }

    @Test
    @DisplayName("Поиск пользователя по id, пользователь не найден")
    void findUserById_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.findUserById(999L));

        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    void findAllUsers_ShouldReturnListOfOne() {
        UserDto savedUser = userService.addUser(userDto);

        List<UserDto> users = userService.findAllUsers();

        assertThat(users, notNullValue());
        assertThat(users, is(List.of(savedUser)));
    }

    @Test
    @DisplayName("Поиск всех пользователей, когда в БД несколько пользователей")
    void findAllUsers_ShouldReturnUserList() {
        UserDto savedUser = userService.addUser(userDto);
        UserDto userDto2 = UserDto.builder()
                .name("username2")
                .email("test2@email.com")
                .build();
        UserDto savedUser2 = userService.addUser(userDto2);

        List<UserDto> users = userService.findAllUsers();

        assertThat(users, notNullValue());
        assertThat(users, is(List.of(savedUser, savedUser2)));
    }

    @Test
    @DisplayName("Поиск всех пользователей, когда в БД нет пользователей")
    void findAllUsers_WithNoUsers_ShouldReturnEmptyList() {

        List<UserDto> users = userService.findAllUsers();

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }

    @Test
    @DisplayName("Удаление пользователя по id")
    void deleteUserById_UserExits_ShouldDeleteUser() {
        UserDto savedUser = userService.addUser(userDto);

        userService.deleteUserById(savedUser.getId());
        List<UserDto> users = userService.findAllUsers();

        assertThat(users, notNullValue());
        assertThat(users, emptyIterable());
    }
}