package ru.practicum.shareit.item.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.QItem;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public ItemDto addItem(final long userId, final ItemDto itemDto) {
        final User owner = getUser(userId);
        final Item item = itemMapper.toModel(itemDto);
        item.setOwner(owner);
        final Item addedItem = itemStorage.save(item);
        log.info("Пользователь с id '{}' добавил новую вещь c id '{}'.", userId, addedItem.getId());
        return itemMapper.toDto(addedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(final long userId, final long itemId, final ItemUpdateDto itemUpdateDto) {
        getUser(userId);
        final Item item = getItem(itemId);
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
        final Item updatedItem = itemStorage.save(item);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto findItemById(final long userId, final long itemId) {
        final Item item = getItem(itemId);
        List<Booking> itemBookings = bookingStorage.findAllByItemId(itemId);
        ItemWithBookingsDto itemWithBookingDatesDto;
        if (item.getOwner().getId() == userId) {
            itemWithBookingDatesDto = getItemWithBookings(item, itemBookings);
        } else {
            itemWithBookingDatesDto = itemMapper.toWithBookingsDto(item);
        }
        log.info("Получение вещи с id '{}'.", itemId);
        return itemWithBookingDatesDto;
    }

    @Override
    public List<ItemWithBookingsDto> findAllItemsByUserId(final long userId) {
        userStorage.findById(userId);
        final List<Item> items = itemStorage.findAllByOwnerIdOrderById(userId);
        final List<Long> itemIds = items.stream()
                .map(Item::getId).collect(Collectors.toList());
        final List<Booking> bookingFromIds = bookingStorage.findAllByItemIdIn(itemIds);
        final List<ItemWithBookingsDto> itemsWithBookings = getItemsWithBookings(items, bookingFromIds);
        log.info("Получение всех вещей пользователя с id '{}'.", userId);
        return itemsWithBookings;
    }

    @Override
    public List<ItemDto> searchItems(final String text) {
        log.info("Поиск вещей по запросу: {}.", text);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        final BooleanExpression nameContains = QItem.item.name.containsIgnoreCase(text);
        final BooleanExpression descriptionContains = QItem.item.description.containsIgnoreCase(text);
        final BooleanExpression isAvailable = QItem.item.available.eq(true);
        final Iterable<Item> searchResult = itemStorage.findAll(nameContains.or(descriptionContains).and(isAvailable));
        return itemMapper.toDtoList(Lists.newArrayList(searchResult));
    }

    private List<ItemWithBookingsDto> getItemsWithBookings(List<Item> items, List<Booking> bookings) {
        if (bookings.isEmpty()) {
            return itemMapper.toWithBookingsDtoList(items);
        }
        final Map<Long, List<Booking>> itemIdToBookings = bookings.stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId(), Collectors.toList()));
        final List<ItemWithBookingsDto> result = new ArrayList<>();
        for (Item item : items) {
            final Long itemId = item.getId();
            List<Booking> itemBookings = itemIdToBookings.computeIfAbsent(itemId, k -> new ArrayList<>());
            final ItemWithBookingsDto itemWithBookingDatesDto = getItemWithBookings(item, itemBookings);
            result.add(itemWithBookingDatesDto);

        }
        return result;
    }

    private ItemWithBookingsDto getItemWithBookings(final Item item, final List<Booking> itemBookings) {
        final Optional<Booking> closestBooking = itemBookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now())
                        && booking.getStatus().equals(BookingStatus.APPROVED))
                .min(Comparator.comparing(Booking::getStart));
        final Optional<Booking> lastBooking = itemBookings.stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()) &&
                        booking.getStatus().equals(BookingStatus.APPROVED))
                .max(Comparator.comparing(Booking::getEnd));
        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(bookingMapper.toShortDto(lastBooking.orElse(null)))
                .nextBooking(bookingMapper.toShortDto(closestBooking.orElse(null)))
                .build();
    }

    private User getUser(final long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }

    private Item getItem(final long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id '" + itemId + "' не найдена."));
    }
}
