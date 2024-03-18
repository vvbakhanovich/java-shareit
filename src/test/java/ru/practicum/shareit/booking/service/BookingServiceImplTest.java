package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.shared.exception.ItemUnavailableException;
import ru.practicum.shareit.shared.exception.NotAuthorizedException;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.booking.dto.GetBookingState.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @Captor
    private ArgumentCaptor<OffsetPageRequest> offsetPageRequestArgumentCaptor;

    private long userId;

    private long bookingId;

    private long itemId;

    private Item item;

    private User itemOwner;

    private User booker;

    private Booking booking;

    @BeforeEach
    void init() {
        userId = 1;
        bookingId = 2;
        itemId = 3;
        itemOwner = User.builder()
                .id(5L)
                .name("name")
                .email("email@mail.com")
                .build();
        item = Item.builder()
                .id(itemId)
                .name("name")
                .description("description")
                .available(true)
                .owner(itemOwner)
                .build();
        booker = User.builder()
                .id(userId)
                .build();
        booking = Booking.builder()
                .status(BookingStatus.WAITING)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .item(item)
                .booker(booker)
                .build();
    }

    @Test
    void addBooking_ItemAndUserFound_ShouldReturnBookingDto() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        User user = new User();
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));

        bookingService.addBooking(userId, addBookingDto);

        verify(userStorage, times(1)).findById(userId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).save(bookingArgumentCaptor.capture());
        Booking captorValue = bookingArgumentCaptor.getValue();

        assertThat(captorValue.getItem(), is(item));
        assertThat(captorValue.getBooker(), is(user));
        assertThat(captorValue.getStatus(), is(BookingStatus.WAITING));
        assertThat(captorValue.getStart(), is(addBookingDto.getStart()));
        assertThat(captorValue.getEnd(), is(addBookingDto.getEnd()));

        verify(bookingMapper, times(1)).toDto(any());
    }

    @Test
    void addBooking_UserNotFound_ShouldThrowNotFoundException() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(userId, addBookingDto));

        assertThat(e.getMessage(), is("Пользователь с id '" + userId + "' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemStorage, never()).findById(any());
        verify(bookingStorage, never()).save(any());
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void addBooking_ItemNotFound_ShouldThrowNotFoundException() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        User user = new User();
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(userId, addBookingDto));

        assertThat(e.getMessage(), is("Вещь с id '" + itemId + "' не найдена."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, never()).save(any());
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void addBooking_OwnerTryToBookHisItem_ShouldThrowNotAuthorizedException() {
        AddBookingDto addBookingDto = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        User user = new User();
        itemOwner.setId(userId);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.addBooking(userId, addBookingDto));

        assertThat(e.getMessage(), is("Вещь с id '" + itemId +
                "' уже принадлежит пользователю с id '" + userId + "'."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, never()).save(any());
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void acknowledgeBooking_UserAndBookingFoundAndSApprovedTrue_ShouldReturnBookingDto() {
        itemOwner.setId(userId);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.acknowledgeBooking(userId, bookingId, true);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, times(1)).toDto(bookingArgumentCaptor.capture());
        Booking captorValue = bookingArgumentCaptor.getValue();

        assertThat(captorValue.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void acknowledgeBooking_UserAndBookingFoundAndApprovedFalse_ShouldReturnBookingDto() {
        itemOwner.setId(userId);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.acknowledgeBooking(userId, bookingId, false);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, times(1)).toDto(bookingArgumentCaptor.capture());
        Booking captorValue = bookingArgumentCaptor.getValue();

        assertThat(captorValue.getStatus(), is(BookingStatus.REJECTED));
    }

    @Test
    void acknowledgeBooking_UserAndBookingFoundBookingStatusNotWaiting_ShouldThrowItemUnavailableException() {
        itemOwner.setId(userId);
        booking.setStatus(BookingStatus.APPROVED);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> bookingService.acknowledgeBooking(userId, bookingId, false));

        assertThat(e.getMessage(), is("Текущий статус бронирования не позволяет сделать подтверждение."));

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void acknowledgeBooking_UserNotFound_ShouldThrowNotFoundException() {
        itemOwner.setId(userId);
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(userId, bookingId, false));

        assertThat(e.getMessage(), is("Пользователь с id '" + userId + "' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, never()).findBookingById(any());
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void acknowledgeBooking_BookingNotFound_ShouldThrowNotFoundException() {
        itemOwner.setId(userId);
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(userId, bookingId, false));

        assertThat(e.getMessage(), is("Бронирование с id '" + bookingId + "' не найдено."));

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void getBookingById_RequesterIsBooker() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.getBookingById(userId, bookingId);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void getBookingById_RequesterIsItemOwner() {
        when(userStorage.findById(itemOwner.getId()))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        bookingService.getBookingById(itemOwner.getId(), bookingId);

        verify(userStorage, times(1)).findById(itemOwner.getId());
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, times(1)).toDto(booking);
    }

    @Test
    void getBookingById_UnauthorizedRequest_ShouldThrowNotAuthorizedException() {
        long unknownUserId = 99L;
        when(userStorage.findById(unknownUserId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.of(booking));

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.getBookingById(unknownUserId, bookingId));
        assertThat(e.getMessage(), is("У пользователя с id '" + unknownUserId + "' нет прав для доступа к бронированию с" +
                " id '" + bookingId + "'."));

        verify(userStorage, times(1)).findById(unknownUserId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void getBookingById_UserNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertThat(e.getMessage(), is("Пользователь с id '" + userId + "' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, never()).findBookingById(any());
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void getBookingById_BookingNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingById(bookingId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(userId, bookingId));
        assertThat(e.getMessage(), is("Бронирование с id '" + bookingId + "' не найдено."));

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingById(bookingId);
        verify(bookingMapper, never()).toDto(any());
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStateAll_ShouldReturnListOfBookings() {
        GetBookingState state = ALL;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findAllByItemOwnerId(eq(userId), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findAllByItemOwnerId(eq(userId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStateCurrent_ShouldReturnListOfBookings() {
        GetBookingState state = CURRENT;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findCurrentBookingsByOwnerId(eq(userId), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findCurrentBookingsByOwnerId(eq(userId), any(), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStatePast_ShouldReturnListOfBookings() {
        GetBookingState state = PAST;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findPastBookingsByOwnerId(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findPastBookingsByOwnerId(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStateFuture_ShouldReturnListOfBookings() {
        GetBookingState state = FUTURE;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findFutureBookingsByOwnerId(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findFutureBookingsByOwnerId(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStateWaiting_ShouldReturnListOfBookings() {
        GetBookingState state = WAITING;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingsByOwnerIdAndStatus(eq(userId), eq(BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingsByOwnerIdAndStatus(eq(userId),
                eq(BookingStatus.WAITING), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsOwnerFromAndSizeAreNotNullStateRejected_ShouldReturnListOfBookings() {
        GetBookingState state = REJECTED;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = true;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingsByOwnerIdAndStatus(eq(userId), eq(BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingsByOwnerIdAndStatus(eq(userId),
                eq(BookingStatus.REJECTED), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStateAll_ShouldReturnListOfBookings() {
        GetBookingState state = ALL;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findAllByBookerId(eq(userId), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findAllByBookerId(eq(userId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStateCurrent_ShouldReturnListOfBookings() {
        GetBookingState state = CURRENT;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findCurrentBookingsByBookerId(eq(userId), any(), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findCurrentBookingsByBookerId(eq(userId), any(), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStatePast_ShouldReturnListOfBookings() {
        GetBookingState state = PAST;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findPastBookingsByBookerId(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findPastBookingsByBookerId(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStateFuture_ShouldReturnListOfBookings() {
        GetBookingState state = FUTURE;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findFutureBookingsByBookerId(eq(userId), any(), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findFutureBookingsByBookerId(eq(userId), any(),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStateWaiting_ShouldReturnListOfBookings() {
        GetBookingState state = WAITING;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingsByBookerIdAndStatus(eq(userId), eq(BookingStatus.WAITING), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingsByBookerIdAndStatus(eq(userId),
                eq(BookingStatus.WAITING), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }

    @Test
    void getAllBookingsFromUser_RequesterIsNotOwnerFromAndSizeAreNotNullStateRejected_ShouldReturnListOfBookings() {
        GetBookingState state = REJECTED;
        Long from = 1L;
        Integer size = 2;
        boolean isOwner = false;
        when(userStorage.findById(eq(userId)))
                .thenReturn(Optional.of(new User()));
        when(bookingStorage.findBookingsByBookerIdAndStatus(eq(userId), eq(BookingStatus.REJECTED), any()))
                .thenReturn(List.of(booking));

        bookingService.getAllBookingsFromUser(userId, state, from, size, isOwner);

        verify(userStorage, times(1)).findById(userId);
        verify(bookingStorage, times(1)).findBookingsByBookerIdAndStatus(eq(userId),
                eq(BookingStatus.REJECTED), offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));

        verify(bookingMapper, times(1)).toDtoList(List.of(booking));
    }
}