package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User user;

    private UserDto userDto;

    private UserUpdateDto updateDto;

    private long userId;

    @BeforeEach
    void setUp() {
        user = new User();
        userDto = UserDto.builder()
                .name("name")
                .email("test@mail.com")
                .build();
        updateDto = UserUpdateDto.builder()
                .name("updated name")
                .email("updated@mail.com")
                .build();
        userId = 1;
    }

    @Test
    void addUser_ValidUser_ShouldReturnUserDto() {
        when(userMapper.toModel(userDto))
                .thenReturn(user);
        when(userStorage.save(user))
                .thenReturn(user);

        userService.addUser(userDto);

        verify(userMapper, times(1)).toModel(userDto);
        verify(userStorage, times(1)).save(user);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void updateUser_UserFoundAndNameNotNullEmailNotNull_ShouldUpdateNameAndEmail() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));

       userService.updateUser(userId, updateDto);

        verify(userStorage).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(updateDto.getName()));
        assertThat(savedUser.getEmail(), is(updateDto.getEmail()));

        verify(userStorage, times(1)).findById(userId);
        verify(userStorage, times(1)).save(savedUser);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.updateUser(userId, updateDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + userId + "' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(userStorage, never()).save(any());
        verify(userMapper, never()).toDto(any());
    }

    @Test
    void updateUser_UserFoundAndNameNullEmailNotNull_ShouldUpdateOnlyEmail() {
        updateDto.setName(null);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));

        userService.updateUser(userId, updateDto);

        verify(userStorage).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(user.getName()));
        assertThat(savedUser.getEmail(), is(updateDto.getEmail()));

        verify(userStorage, times(1)).findById(userId);
        verify(userStorage, times(1)).save(savedUser);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void updateUser_UserFoundAndNameNotNullEmailNull_ShouldUpdateOnlyName() {
        updateDto.setEmail(null);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));

        userService.updateUser(userId, updateDto);

        verify(userStorage).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(updateDto.getName()));
        assertThat(savedUser.getEmail(), is(user.getEmail()));

        verify(userStorage, times(1)).findById(userId);
        verify(userStorage, times(1)).save(savedUser);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void updateUser_UserFoundAndNameNullEmailNull_ShouldNotUpdateAnyFields() {
        updateDto.setEmail(null);
        updateDto.setName(null);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));

        userService.updateUser(userId, updateDto);

        verify(userStorage).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getName(), is(user.getName()));
        assertThat(savedUser.getEmail(), is(user.getEmail()));

        verify(userStorage, times(1)).findById(userId);
        verify(userStorage, times(1)).save(savedUser);
        verify(userMapper, times(1)).toDto(savedUser);
    }

    @Test
    void findUserById_UserFound_ShouldReturnDto() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));

        userService.findUserById(userId);

        verify(userStorage, times(1)).findById(userId);
        verify(userMapper, times(1)).toDto(user);
    }

    @Test
    void findUserById_UserNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> userService.findUserById(userId));
        assertThat(e.getMessage(), is("Пользователь с id '" + userId + "' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(userMapper, never()).toDto(user);
    }

    @Test
    void findAllUsers_ShouldReturnList() {
        when(userStorage.findAll())
                .thenReturn(List.of(user));

        userService.findAllUsers();

        verify(userStorage, times(1)).findAll();
        verify(userMapper, times(1)).toDtoList(List.of(user));
    }

    @Test
    void deleteUserById() {
        userService.deleteUserById(userId);

        verify(userStorage, times(1)).deleteById(userId);
    }
}