package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addNewItemRequest(Long userId, AddItemRequestDto addItemRequestDto);

    List<ItemRequestDto> getAllItemRequestsFromUser(Long userId);

    List<ItemRequestDto> getAllItemRequests(Long userId, Long from, Integer size);

    ItemRequestDto getItemRequestById(Long userId, Long requestId);
}
