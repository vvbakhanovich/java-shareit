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

    /**
     * Добавление нового пользователя.
     *
     * @param userDto добавляемый пользователь
     * @return добавленный пользователь
     */
    @Override
    @Transactional
    public UserDto addUser(final UserDto userDto) {
        final User user = userMapper.toModel(userDto);
        final User addedUser = userStorage.save(user);
        log.info("Добавлен новый пользователя с id '{}'.", addedUser.getId());
        return userMapper.toDto(addedUser);
    }

    /**
     * Обновление данных пользователя. Разрешено обновлять имя и электронную почту.
     *
     * @param userId        идентификатор пользователя
     * @param userUpdateDto обновленные данные
     * @return обновленный пользователь
     */
    @Override
    @Transactional
    public UserDto updateUser(final long userId, final UserUpdateDto userUpdateDto) {
        User storedUser = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
        updateNameAndEmail(userUpdateDto, storedUser);
        userStorage.save(storedUser);
        log.info("Обновление пользователя с id '{}'.", userId);
        return userMapper.toDto(storedUser);
    }

    /**
     * Поиск пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return найденный пользователь
     */
    @Override
    public UserDto findUserById(final long userId) {
        final User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
        log.info("Получение пользователя с id '{}.", userId);
        return userMapper.toDto(user);
    }

    /**
     * Получение списка всех пользователей.
     *
     * @return список всех пользователей
     */
    @Override
    public List<UserDto> findAllUsers() {
        final List<User> users = userStorage.findAll();
        log.info("Получение списка всех пользователей.");
        return userMapper.toDtoList(users);
    }

    /**
     * Удаление пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     */
    @Override
    public void deleteUserById(final long userId) {
        userStorage.deleteById(userId);
        log.info("Удаление пользователя с id '{}'.", userId);
    }

    private void updateNameAndEmail(UserUpdateDto userUpdateDto, User storedUser) {
        if (userUpdateDto.getName() != null) {
            storedUser.setName(userUpdateDto.getName());
        }
        if (userUpdateDto.getEmail() != null) {
            storedUser.setEmail(userUpdateDto.getEmail());
        }
    }
}
