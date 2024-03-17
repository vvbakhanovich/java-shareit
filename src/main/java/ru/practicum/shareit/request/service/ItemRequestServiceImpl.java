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

    /**
     * Добавление нового запроса вещи.
     *
     * @param userId            идентификатор пользователя, делающего запрос
     * @param addItemRequestDto описание запрашиваемой вещи
     * @return заполненный запрос
     */
    @Override
    public ItemRequestDto addNewItemRequest(final Long userId, final AddItemRequestDto addItemRequestDto) {
        final User requester = findUser(userId);
        final ItemRequest itemRequest = itemRequestMapper.toModel(addItemRequestDto);
        itemRequest.setRequester(requester);
        final ItemRequest savedRequest = itemRequestStorage.save(itemRequest);
        log.info("Добавлен новый запрос с id '{}'.", savedRequest.getId());
        return itemRequestMapper.toDto(savedRequest);
    }

    /**
     * Получение списка своих запросов вместе с данными ответа на них. Запросы возвращаются в отсортированном порядке
     * от более новых к более старым.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @return список запросов
     */
    @Override
    public List<ItemRequestDto> getAllItemRequestsFromUser(final Long userId) {
        findUser(userId);
        final List<ItemRequest> requests = itemRequestStorage.findRequestsFromUser(userId);
        log.info("Получение всех запросов для пользователя с id '" + userId + "'.");
        return itemRequestMapper.toDtoList(requests);
    }

    /**
     * Получение списка запросов, созданных другими пользователями. Запросы сортируются по дате создания: от более новых
     * к более старым. Результат возвращается постранично. Для этого указываются два параметра:  from — индекс первого
     * элемента, начиная с 0, и size — количество элементов для отображения.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @param from   индекс первого отображаемого элемента, начиная с 0
     * @param size   количество элементов для отображения
     * @return список запросов
     */
    @Override
    public List<ItemRequestDto> getAvailableItemRequests(final Long userId, final Long from, final Integer size) {
        findUser(userId);
        if (from == null && size == null) {
            return itemRequestMapper.toDtoList(itemRequestStorage.findAllRequests());
        }
        final OffsetPageRequest pageRequest = OffsetPageRequest.of(from, size);
        final Page<ItemRequest> requests = itemRequestStorage.findAvailableRequests(userId, pageRequest);
        log.info("Получение списка запросов, начиная с '{}', по '{}' элемента на странице.", from, size);
        return itemRequestMapper.toDtoList(requests.getContent());
    }

    /**
     * Получение данных о конкретном запросе вместе с с данными об ответах на него Посмотреть данные об отдельном
     * запросе может любой пользователь.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @param requestId идентификатор запроса
     * @return найденный запрос
     */
    @Override
    public ItemRequestDto getItemRequestById(final Long userId, final Long requestId) {
        findUser(userId);
        final ItemRequest itemRequest = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id '" + requestId + "' не найден."));
        log.info("Получение запроса с id '{}'.", requestId);
        return itemRequestMapper.toDto(itemRequest);
    }

    private User findUser(final Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }
}
