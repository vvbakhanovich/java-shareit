package ru.practicum.shareit.item.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.shared.IdGenerator;
import ru.practicum.shareit.shared.exception.NotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ItemStorageImpl implements ItemStorage {

    private final IdGenerator<Long> idGenerator;
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Item>> userItems = new LinkedHashMap<>();

    @Override
    public Item save(final long userId, final Item item) {
        final Long id = idGenerator.generateId();
        item.setId(id);
        items.put(id, item);
        List<Item> userItemsList = userItems.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>());
        userItemsList.add(item);
        userItems.put(userId, userItemsList);
        return item;
    }

    @Override
    public Item findById(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id '" + itemId + "' не найдена.");
        }
        return items.get(itemId);
    }

    @Override
    public Item update(long itemId, ItemUpdateDto itemUpdateDto) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Вещь с id '" + itemId + "' не найдена.");
        }
        Item storedItem = items.get(itemId);
        if (itemUpdateDto.getName() != null) {
            storedItem.setName(itemUpdateDto.getName());
        }
        if (itemUpdateDto.getDescription() != null) {
            storedItem.setDescription(itemUpdateDto.getDescription());
        }
        if (itemUpdateDto.getAvailable() != null) {
            storedItem.setAvailable(itemUpdateDto.getAvailable());
        }
        return storedItem;
    }

    @Override
    public List<Item> findAllByUserId(long userId) {
        return userItems.getOrDefault(userId, new ArrayList<>());
    }

    @Override
    public List<Item> search(String text) {
        if (text.isEmpty()) {
            return Collections.emptyList();
        }
        return items.values().stream()
                .filter((item -> item.getName().toLowerCase().contains(text) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.isAvailable()))
                .collect(Collectors.toList());
    }
}
