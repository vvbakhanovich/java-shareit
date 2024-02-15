package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item save(long userId, Item itemDto);

    Item findById(long itemId);

    Item update(long itemId, ItemUpdateDto itemUpdateDto);

    List<Item> findAllByUserId(long userId);

    List<Item> search(String text);
}
