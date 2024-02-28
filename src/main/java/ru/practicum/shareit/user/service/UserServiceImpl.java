package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto addUser(final UserDto userDto) {
        final User user = userMapper.toModel(userDto);
        final User addedUser = userStorage.save(user);
        log.info("Добавлен новый пользователя с id '{}'.", addedUser.getId());
        return userMapper.toDto(addedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(final long userId, final UserUpdateDto userUpdateDto) {
        User storedUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
        if (userUpdateDto.getName() != null) {
            storedUser.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null) {
            storedUser.setEmail(userUpdateDto.getEmail());
        }
        log.info("Обновление пользователя с id '{}'.", userId);
        userStorage.save(storedUser);
        return userMapper.toDto(storedUser);
    }

    @Override
    public UserDto findUserById(final long userId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
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
