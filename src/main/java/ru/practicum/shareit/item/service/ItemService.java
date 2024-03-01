package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(long userId, ItemDto itemDto);

    ItemDto updateItem(long userId, long itemId, ItemUpdateDto itemUpdateDto);

    ItemWithBookingsDto findItemById(long userId, long itemId);

    List<ItemWithBookingsDto> findAllItemsByUserId(long userId);

    List<ItemDto> searchItems(String text);
}
