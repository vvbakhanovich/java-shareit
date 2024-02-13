package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@Service
public interface UserService {
    UserDto addUser(UserDto userDto);

    UserDto updateUser(long userId, UserUpdateDto userUpdateDto);

    UserDto findUserById(long userId);

    List<UserDto> findAllUsers();

    void deleteUserById(long userId);
}
