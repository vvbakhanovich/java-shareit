package ru.practicum.shareit.booking.service;

import com.google.common.collect.Lists;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.model.QBooking;
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

    @Override
    public List<BookingDto> getAllOwnerBookings(final Long userId, final GetBookingState state) {
        findUser(userId);
        final Iterable<Booking> result = new ArrayList<>();
        final BooleanExpression byOwnerId = QBooking.booking.item.owner.id.eq(userId);
        final Sort sortByStartAsc = Sort.by(Sort.Direction.DESC, "start");
        final Iterable<Booking> allOwnerBookings = getAllSortedBookingsFromUser(state, result, byOwnerId, sortByStartAsc);
        return bookingMapper.toDtoList(Lists.newArrayList(allOwnerBookings));
    }

    @Override
    public List<BookingDto> getAllBookingsFromUser(final Long userId, final GetBookingState state) {
        findUser(userId);
        Iterable<Booking> result = new ArrayList<>();
        final Sort sortByStartAsc = Sort.by(Sort.Direction.DESC, "start");
        final BooleanExpression byUserId = QBooking.booking.booker.id.eq(userId);
        result = getAllSortedBookingsFromUser(state, result, byUserId, sortByStartAsc);
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

    private Iterable<Booking> getAllSortedBookingsFromUser(final GetBookingState state, Iterable<Booking> result,
                                                           final BooleanExpression byUserId, final Sort sortByStartAsc) {
        if (GetBookingState.ALL.equals(state)) {
            result = bookingStorage.findAll(byUserId, sortByStartAsc);
        }
        if (GetBookingState.CURRENT.equals(state)) {
            BooleanExpression startBeforeNow = QBooking.booking.start.before(LocalDateTime.now());
            BooleanExpression endAfterNow = QBooking.booking.end.after(LocalDateTime.now());
            result = bookingStorage.findAll(byUserId.and(startBeforeNow).and(endAfterNow),
                    sortByStartAsc);
        }
        if (GetBookingState.PAST.equals(state)) {
            BooleanExpression ended = QBooking.booking.end.before(LocalDateTime.now());
            result = bookingStorage.findAll(byUserId.and(ended), sortByStartAsc);
        }
        if (GetBookingState.FUTURE.equals(state)) {
            BooleanExpression futureStart = QBooking.booking.start.after(LocalDateTime.now());
            result = bookingStorage.findAll(byUserId.and(futureStart), sortByStartAsc);
        }
        if (GetBookingState.WAITING.equals(state)) {
            BooleanExpression waitingStatus = QBooking.booking.status.eq(BookingStatus.WAITING);
            result = bookingStorage.findAll(byUserId.and(waitingStatus), sortByStartAsc);
        }
        if (GetBookingState.REJECTED.equals(state)) {
            BooleanExpression rejectedStatus = QBooking.booking.status.eq(BookingStatus.REJECTED);
            result = bookingStorage.findAll(byUserId.and(rejectedStatus), sortByStartAsc);
        }
        return result;
    }
}
