package ru.practicum.shareit.booking.service;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.shared.exception.ItemUnavailableException;
import ru.practicum.shareit.shared.exception.NotAuthorizedException;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;
    private final BookingMapper bookingMapper;


    /**
     * Добавление нового запроса на бронирование. Запрос может быть создан любым пользователем.
     *
     * @param userId     идентификатор пользователя, делающего бронирование
     * @param bookingDto объект бронирования
     * @return бронирование с присвоенным идентификатором
     */
    @Override
    @Transactional
    public BookingDto addBooking(final Long userId, final AddBookingDto bookingDto) {
        final User user = findUser(userId);
        final Item item = itemStorage.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id '" + bookingDto.getItemId() + "' не найдена."));
        checkItemAvailability(item);
        if (item.getOwner().getId().equals(userId)) {
            throw new NotAuthorizedException("Вещь с id '" + item.getId() +
                    "' уже принадлежит пользователю с id '" + userId + "'.");
        }
        final Booking booking = Booking.builder()
                .item(item)
                .booker(user)
                .status(BookingStatus.WAITING)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
        final Booking savedBooking = bookingStorage.save(booking);
        log.info("Пользователь с id '{}' добавил бронирование вещи с id '{}'.", userId, bookingDto.getItemId());
        return bookingMapper.toDto(savedBooking);
    }

    /**
     * Подтверждение или отклонение запроса на бронирование. Может быть выполнено только владельцем вещи.
     *
     * @param userId    идентификатор пользователя, делающего подтверждение
     * @param bookingId идентификатор бронирования
     * @param approved  подтверждение или отмена бронирования
     * @return подтвержденное или отмененное бронирование
     */
    @Override
    @Transactional
    public BookingDto acknowledgeBooking(final Long userId, final Long bookingId, final Boolean approved) {
        findUser(userId);
        final Booking booking = findBooking(bookingId);
        final Item item = booking.getItem();
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotAuthorizedException("Пользователь с id '" + userId +
                    "' не является владельцем вещи с id '" + item.getId() + "'.");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ItemUnavailableException("Вещь уже находится в аренде.");
        }
        if (approved) {
            booking.setStatus(BookingStatus.APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }
        return bookingMapper.toDto(booking);
    }

    /**
     * Получение бронирования по идентификатору.Может быть выполнено либо автором бронирования, либо владельцем вещи,
     * к которой относится бронирование.
     *
     * @param userId    идентификатор пользователя, делающего запрос
     * @param bookingId идентификатор бронирования
     * @return найденное бронирование
     */
    @Override
    public BookingDto getBookingById(final Long userId, final Long bookingId) {
        findUser(userId);
        final Booking booking = findBooking(bookingId);
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return bookingMapper.toDto(booking);
        } else {
            throw new NotAuthorizedException("У пользователя с id '" + userId + "' нет прав для доступа к бронированию с" +
                    " id '" + bookingId + "'.");
        }
    }

    /**
     * Получение списка всех бронирований текущего пользователя. Параметр state необязательный и по умолчанию равен ALL.
     * Также он может принимать значения CURRENT, PAST, FUTURE, WAITING, REJECTED. Бронирования возвращаются
     * отсортированными по дате от более новых к более старым.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @param state  статус бронирования
     * @return список бронирований
     */
    @Override
    public List<BookingDto> getAllOwnerBookings(final Long userId, final GetBookingState state) {
        findUser(userId);
        final Iterable<Booking> result = new ArrayList<>();
        final Iterable<Booking> allOwnerBookings = getAllSortedBookingsFromUser(state, result, userId);
        return bookingMapper.toDtoList(Lists.newArrayList(allOwnerBookings));
    }

    /**
     * Получение списка бронирований для всех вещей текущего пользователя. Параметр state необязательный и по умолчанию
     * равен ALL. Также он может принимать значения CURRENT, PAST, FUTURE, WAITING, REJECTED. Бронирования возвращаются
     * отсортированными по дате от более новых к более старым.
     *
     * @param userId идентификатор пользователя, делающего запрос
     * @param state  статус бронирования
     * @return список бронирований
     */
    @Override
    public List<BookingDto> getAllBookingsFromUser(final Long userId, final GetBookingState state) {
        findUser(userId);
        Iterable<Booking> result = new ArrayList<>();
        result = getAllSortedBookingsFromBooker(state, result, userId);
        return bookingMapper.toDtoList(Lists.newArrayList(result));
    }

    private User findUser(final Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id '" + userId + "' не найден."));
    }

    private Booking findBooking(final Long bookingId) {
        return bookingStorage.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id '" + bookingId + "' не найдено."));
    }

    private void checkItemAvailability(final Item item) {
        if (!item.getAvailable()) {
            throw new ItemUnavailableException("Вещь недоступна для бронирования.");
        }
    }

    /*
        Изначально сделал через query dsl, получилось лаконично и красиво. Но в процессе дебага увидел, что findAll()
        выгружает данные через N+1. Поэтому пришлось плодить подобные методы, но зато появился контроль за выгрузкой.
        Может есть способ сделать это более красиво? Еще, как оказалось, тесты на gitHub не пропускают вариант с queryDsl
        так как папка generated-sources не помечена как generated source folder и классы QBooking и подобные не видятся
     */
    private Iterable<Booking> getAllSortedBookingsFromUser(final GetBookingState state, Iterable<Booking> result,
                                                           final Long userId) {
        if (GetBookingState.ALL.equals(state)) {
            result = bookingStorage.findAllByItemOwnerIdOrderByStartDesc(userId);
        }
        if (GetBookingState.CURRENT.equals(state)) {
            result = bookingStorage.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, LocalDateTime.now(),
                    LocalDateTime.now());
        }
        if (GetBookingState.PAST.equals(state)) {
            result = bookingStorage.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, LocalDateTime.now());
        }
        if (GetBookingState.FUTURE.equals(state)) {
            result = bookingStorage.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, LocalDateTime.now());
        }
        if (GetBookingState.WAITING.equals(state)) {
            result = bookingStorage.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING);
        }
        if (GetBookingState.REJECTED.equals(state)) {
            result = bookingStorage.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED);
        }
        return result;
    }

    private Iterable<Booking> getAllSortedBookingsFromBooker(final GetBookingState state, Iterable<Booking> result,
                                                             final Long bookerId) {
        if (GetBookingState.ALL.equals(state)) {
            result = bookingStorage.findAllByBookerIdOrderByStartDesc(bookerId);
        }
        if (GetBookingState.CURRENT.equals(state)) {
            result = bookingStorage.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, LocalDateTime.now(),
                    LocalDateTime.now());
        }
        if (GetBookingState.PAST.equals(state)) {
            result = bookingStorage.findAllByBookerIdAndEndBeforeOrderByStartDesc(bookerId, LocalDateTime.now());
        }
        if (GetBookingState.FUTURE.equals(state)) {
            result = bookingStorage.findAllByBookerIdAndStartAfterOrderByStartDesc(bookerId, LocalDateTime.now());
        }
        if (GetBookingState.WAITING.equals(state)) {
            result = bookingStorage.findByBookerIdAndStatus(bookerId, BookingStatus.WAITING);
        }
        if (GetBookingState.REJECTED.equals(state)) {
            result = bookingStorage.findByBookerIdAndStatus(bookerId, BookingStatus.REJECTED);
        }
        return result;
    }
}
