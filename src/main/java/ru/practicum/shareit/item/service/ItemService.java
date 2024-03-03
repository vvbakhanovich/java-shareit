package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.GetItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemUpdateDto itemUpdateDto);

    GetItemDto findItemById(Long userId, Long itemId);

    List<GetItemDto> findAllItemsByUserId(Long userId);

    List<ItemDto> searchItems(String text);

    CommentDto addCommentToItem(Long userId, Long itemId, AddCommentDto commentDto);
}
