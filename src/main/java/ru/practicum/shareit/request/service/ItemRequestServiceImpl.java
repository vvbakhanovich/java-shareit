package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestStorage itemRequestStorage;
    private final UserStorage userStorage;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    public ItemRequestDto addNewItemRequest(Long userId, AddItemRequestDto addItemRequestDto) {
        User requester = findUser(userId);
        ItemRequest itemRequest = itemRequestMapper.toModel(addItemRequestDto);
        itemRequest.setRequester(requester);
        ItemRequest savedRequest = itemRequestStorage.save(itemRequest);
        log.info("Добавлен новый запрос с id '{}'.", savedRequest.getId());
        return itemRequestMapper.toDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequestsFromUser(Long userId) {
        findUser(userId);
        List<ItemRequest> requests = itemRequestStorage.findRequestsFromUser(userId);
        log.info("Получение всех запросов для пользователя с id '" + userId + "'.");
        return itemRequestMapper.toDtoList(requests);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, Long from, Integer size) {
        findUser(userId);
        if (from == null && size == null) {
            return itemRequestMapper.toDtoList(itemRequestStorage.findAll());
        }
        OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(userId, pageRequest);
        log.info("Получение списка запросов, начиная с '{}', по '{}' элемента на странице.", from, size);
        return itemRequestMapper.toDtoList(requests.getContent());
    }

    @Override
    public ItemRequestDto getItemRequestById(Long userId, Long requestId) {
        findUser(userId);
        ItemRequest itemRequest = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id '" + requestId + "' не найден."));
        log.info("Получение запроса с id '{}'.", requestId);
        return itemRequestMapper.toDto(itemRequest);
    }

    private User findUser(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }
}
