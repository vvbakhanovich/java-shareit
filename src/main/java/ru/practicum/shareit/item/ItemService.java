package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId);

    ItemDto findItemById(long itemId);

    List<ItemDto> findAllItemsByUserId(long userId);

    List<ItemDto> searchItems(String text);
}
