package ru.practicum.shareit.item.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto addItem(long userId, ItemDto itemDto) {
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
        Item item = itemMapper.toModel(itemDto);
        item.setOwner(owner);
        Item addedItem = itemStorage.save(item);
        log.info("Пользователь с id '{}' добавил новую вещь c id '{}'.", userId, addedItem.getId());
        return itemMapper.toDto(addedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(long userId, long itemId, ItemUpdateDto itemUpdateDto) {
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id '" + itemId + "' не найдена."));
        if (item.getOwner().getId() != userId) {
            throw new NotFoundException("У пользователя с id '" + userId + "' не найдена вещь с id '" + itemId + "'.");
        }
        if (itemUpdateDto.getName() != null) {
            item.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            item.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            item.setAvailable(itemUpdateDto.getAvailable());
        }
        Item updatedItem = itemStorage.save(item);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto findItemById(long itemId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id '" + itemId + "' не найдена."));
        log.info("Получение вещи с id '{}'.", itemId);
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> findAllItemsByUserId(long userId) {
        userStorage.findById(userId);
        List<Item> items = itemStorage.findAllByOwnerId(userId);
        log.info("Получение всех вещей пользователя с id '{}'.", userId);
        return itemMapper.toDtoList(items);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по запросу: {}.", text);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        BooleanExpression nameContains = QItem.item.name.containsIgnoreCase(text);
        BooleanExpression descriptionContains = QItem.item.description.containsIgnoreCase(text);
        BooleanExpression isAvailable = QItem.item.available.eq(true);
        Iterable<Item> searchResult = itemStorage.findAll(nameContains.or(descriptionContains).and(isAvailable));
        return itemMapper.toDtoList(Lists.newArrayList(searchResult));
    }
}
