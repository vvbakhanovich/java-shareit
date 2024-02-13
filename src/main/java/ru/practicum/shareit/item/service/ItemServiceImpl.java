package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static ru.practicum.shareit.item.mapper.ItemMapper.INSTANCE;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemDto addItem(long userId, ItemDto itemDto) {
        userStorage.findById(userId);
        Item item = INSTANCE.toModel(itemDto);
        item.setOwner(userId);
        userStorage.addItemToUser(userId, item.getId());
        Item addedItem = itemStorage.save(item);
        log.info("Пользователь с id '{}' добавил новую вещь: {}.", userId, addedItem);
        return INSTANCE.toDto(addedItem);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemUpdateDto itemUpdateDto) {
        userStorage.findById(userId);
        Item item = itemStorage.findById(itemId);
        if (item.getOwner() != userId) {
            throw new NotFoundException("У пользователя с id '" + userId + "' не найдена вещь с id '" + itemId + "'.");
        }
        Item updatedItem = itemStorage.update(itemId, itemUpdateDto);
        return INSTANCE.toDto(updatedItem);
    }

    @Override
    public ItemDto findItemById(long itemId) {
        Item item = itemStorage.findById(itemId);
        log.info("Получение вещи с id '{}': {}.", itemId, item);
        return INSTANCE.toDto(item);
    }

    @Override
    public List<ItemDto> findAllItemsByUserId(long userId) {
        userStorage.findById(userId);
        List<Item> items = itemStorage.findAllByUserId(userId);
        log.info("Получение всех вещей пользователя с id '{}'.", userId);
        return INSTANCE.toDtoList(items);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        List<Item> searchResult = itemStorage.search(text);
        log.info("Поиск вещей по запросу: {}.", text);
        return INSTANCE.toDtoList(searchResult);
    }
}
