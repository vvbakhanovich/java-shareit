package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {
    User save(User user);

    User update(long userId, UserUpdateDto updateField);

    User findById(long userId);

    List<User> findAll();

    void deleteById(long userId);

    User addItemToUser(long userId, Item item);
}
