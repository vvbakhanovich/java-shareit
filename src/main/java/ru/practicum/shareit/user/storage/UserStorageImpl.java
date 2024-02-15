package ru.practicum.shareit.user.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.shared.IdGenerator;
import ru.practicum.shareit.shared.exception.EmailAlreadyExists;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserStorageImpl implements UserStorage {

    private final IdGenerator<Long> idGenerator;
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User save(final User user) {
        if (users.values().stream().anyMatch(user1 -> user1.getEmail().equals(user.getEmail()))) {
            throw new EmailAlreadyExists("Пользователь c email '" + user.getEmail() + "' уже существует.");
        }
        final Long id = idGenerator.generateId();
        user.setId(id);
        users.put(id, user);
        return user;
    }

    @Override
    public User update(final long userId, final UserUpdateDto updateField) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id '" + userId + "' не найден.");
        }
        User storedUser = users.get(userId);
        if (updateField.getName() != null) {
            storedUser.setName(updateField.getName());
        }
        if (checkEmailIsNotUsed(storedUser, updateField.getEmail())) {
            storedUser.setEmail(updateField.getEmail());
        }
        return storedUser;
    }

    @Override
    public User findById(final long userId) {
        if (users.containsKey(userId)) {
            return users.get(userId);
        } else {
            throw new NotFoundException("Пользователь с id '" + userId + "' не найден.");
        }
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(final long userId) {
        if (users.containsKey(userId)) {
            users.remove(userId);
        } else {
            throw new NotFoundException("Пользователь с id '" + userId + "' не найден.");
        }
    }

    @Override
    public User addItemToUser(final long userId, final Item item) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("Пользователь с id '" + userId + "' не найден.");
        }
        final User user = users.get(userId);
        user.getItems().add(item);
        return user;
    }

    private boolean checkEmailIsNotUsed(final User user, final String email) {
        if (email == null) {
            return false;
        } else if (users.values().stream().noneMatch(storedUser -> storedUser.getEmail().equals(email))
                || user.getEmail().equals(email)) {
            return true;
        } else {
            throw new EmailAlreadyExists("Пользователь c email '" + email + "' уже существует.");
        }
    }
}
