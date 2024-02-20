package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    public UserDto addUser(final UserDto userDto) {
        final User user = userMapper.toModel(userDto);
        final User addedUser = userStorage.save(user);
        log.info("Добавлен новый пользователя с id '{}'.", addedUser.getId());
        return userMapper.toDto(addedUser);
    }

    @Override
    public UserDto updateUser(final long userId, final UserUpdateDto userUpdateDto) {
        final User updatedUserFromDb = userStorage.update(userId, userUpdateDto);
        log.info("Обновление пользователя с id '{}'.", userId);
        return userMapper.toDto(updatedUserFromDb);
    }

    @Override
    public UserDto findUserById(final long userId) {
        final User user = userStorage.findById(userId);
        log.info("Получение пользователя с id '{}.", userId);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> findAllUsers() {
        final List<User> users = userStorage.findAll();
        log.info("Получение списка всех пользователей.");
        return userMapper.toDtoList(users);
    }

    @Override
    public void deleteUserById(final long userId) {
        userStorage.deleteById(userId);
        log.info("Удаление пользователя с id '{}'.", userId);
    }
}
