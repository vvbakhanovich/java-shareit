package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.GetItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentStorage;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.shared.exception.ItemUnavailableException;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemStorage itemStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private BookingStorage bookingStorage;

    @Mock
    private CommentStorage commentStorage;

    @Mock
    private ItemRequestStorage itemRequestStorage;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Captor
    private ArgumentCaptor<Item> itemArgumentCaptor;

    @Captor
    private ArgumentCaptor<Booking> bookingArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> stringArgumentCaptor;

    @Captor
    private ArgumentCaptor<Comment> commentArgumentCaptor;

    @Captor
    private ArgumentCaptor<OffsetPageRequest> offsetPageRequestArgumentCaptor;

    private User owner;

    private long ownerId;

    private User requester;

    private long requesterId;

    private ItemDto itemDto;

    private Item item;

    private long itemId;

    long requestId;

    private Booking booking1;

    private Booking booking2;

    private Booking booking3;

    @BeforeEach
    void setUp() {
        ownerId = 1;
        owner = User.builder()
                .id(ownerId)
                .name("owner")
                .email("owner@email.com")
                .build();
        requesterId = 3;
        requester = User.builder()
                .id(requesterId)
                .name("requester")
                .email("requester@email.com")
                .build();
        requestId = 2;
        itemDto = ItemDto.builder()
                .name("itemDto")
                .description("itemDto description")
                .requestId(requestId)
                .available(true)
                .build();
        itemId = 4;
        item = Item.builder()
                .id(itemId)
                .name("item name")
                .description("item description")
                .owner(owner)
                .available(true)
                .build();
        booking1 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .status(BookingStatus.WAITING)
                .build();
        booking2 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .status(BookingStatus.WAITING)
                .build();
        booking3 = Booking.builder()
                .booker(requester)
                .item(item)
                .start(LocalDateTime.now().plusDays(5))
                .end(LocalDateTime.now().plusDays(6))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    @DisplayName("Добавление вещи")
    void addItem_UserAndRequestFound_ShouldReturnItemDtoWithOwnerAndRequest() {
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        ItemRequest itemRequest = ItemRequest.builder()
                .requester(requester)
                .description("description")
                .build();
        when(itemRequestStorage.findById(requestId))
                .thenReturn(Optional.of(itemRequest));
        when(itemMapper.toModel(itemDto))
                .thenReturn(item);
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.addItem(ownerId, itemDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemRequestStorage, times(1)).findById(requestId);
        verify(itemMapper, times(1)).toModel(itemDto);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getOwner(), is(owner));
        assertThat(captorValue.getRequest(), is(itemRequest));
        assertThat(itemRequest.getItems(), is(List.of(captorValue)));
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Добавление вещи не по запросу")
    void addItem_RequestIdIsNull_ShouldReturnItemDtoWithOwnerAndWithoutRequest() {
        itemDto.setRequestId(null);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemMapper.toModel(itemDto))
                .thenReturn(item);
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.addItem(ownerId, itemDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemRequestStorage, never()).findById(any());
        verify(itemMapper, times(1)).toModel(itemDto);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getOwner(), is(owner));
        assertThat(captorValue.getRequest(), nullValue());
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Добавление вещи, пользователь не найден")
    void addItem_UserNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addItem(ownerId, itemDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + ownerId + "' не найден."));

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemRequestStorage, never()).findById(any());
        verify(itemMapper, never()).toModel(any());
        verify(itemStorage, never()).save(any());
        verify(itemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Добавление вещи по запросу, запрос не найден")
    void addItem_RequestNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemRequestStorage.findById(requestId))
                .thenReturn(Optional.empty());
        when(itemMapper.toModel(itemDto))
                .thenReturn(item);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addItem(ownerId, itemDto));
        assertThat(e.getMessage(), is("Запрос с id '" + requestId + "' не найден."));

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemRequestStorage, times(1)).findById(requestId);
        verify(itemMapper, times(1)).toModel(itemDto);
        verify(itemStorage, never()).save(any());
        verify(itemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи")
    void updateItem_WhenAllUpdateFieldsNotNull_ShouldUpdateNameDescriptionAndAvailable() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(false)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.updateItem(ownerId, itemId, itemUpdateDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemUpdateDto.getName()));
        assertThat(captorValue.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemUpdateDto.getAvailable()));
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового названия")
    void updateItem_WhenUpdatedNameIsNull_ShouldUpdateDescriptionAndAvailable() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name(null)
                .description("new description")
                .available(false)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.updateItem(ownerId, itemId, itemUpdateDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(item.getName()));
        assertThat(captorValue.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemUpdateDto.getAvailable()));
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Обновление данных о вещи без нового описания")
    void updateItem_WhenUpdatedDescriptionIsNull_ShouldUpdateNameAndAvailable() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description(null)
                .available(false)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.updateItem(ownerId, itemId, itemUpdateDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemUpdateDto.getName()));
        assertThat(captorValue.getDescription(), is(item.getDescription()));
        assertThat(captorValue.getAvailable(), is(itemUpdateDto.getAvailable()));
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Обновление данных о вещи без статуса доступности")
    void updateItem_WhenUpdatedAvailableIsNull_ShouldUpdateNameAndDescription() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(itemStorage.save(any()))
                .thenReturn(item);

        itemService.updateItem(ownerId, itemId, itemUpdateDto);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, times(1)).save(itemArgumentCaptor.capture());
        Item captorValue = itemArgumentCaptor.getValue();
        assertThat(captorValue.getName(), is(itemUpdateDto.getName()));
        assertThat(captorValue.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(captorValue.getAvailable(), is(item.getAvailable()));
        verify(itemMapper, times(1)).toDto(item);
    }

    @Test
    @DisplayName("Обновление данных о вещи не владельцем")
    void updateItem_WhenNotOwnerTryToUpdate_ShouldThrowNotFoundException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(requesterId, itemId, itemUpdateDto));
        assertThat(e.getMessage(), is("У пользователя с id '" + requesterId + "' не найдена вещь с id '" +
                itemId + "'."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, never()).save(any());
        verify(itemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи, пользователь не найден")
    void updateItem_WhenUserNotFound_ShouldThrowNotFoundException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(requesterId, itemId, itemUpdateDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + requesterId + "' не найден."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, never()).findById(any());
        verify(itemStorage, never()).save(any());
        verify(itemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Обновление данных о вещи, вещь не найдена")
    void updateItem_WhenItemNotFound_ShouldThrowNotFoundException() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(null)
                .build();
        item.setOwner(owner);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(requesterId, itemId, itemUpdateDto));
        assertThat(e.getMessage(), is("Вещь с id '" + itemId + "' не найдена."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(itemStorage, never()).save(any());
        verify(itemMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос от владельца")
    void findItemById_WhenRequesterIsOwner_ShouldReturnItemWithBookingDates() {
        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setStatus(BookingStatus.APPROVED);
        booking3.setStatus(BookingStatus.APPROVED);
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemId(itemId))
                .thenReturn(List.of(booking1, booking2, booking3));
        Comment comment = new Comment();
        when(commentStorage.findAllByItemId(itemId))
                .thenReturn(List.of(comment));
        ShortBookingDto shortBookingDto = new ShortBookingDto();
        when(bookingMapper.toShortDto(any()))
                .thenReturn(shortBookingDto);
        when(itemMapper.toGetItemDto(eq(item), any(), any()))
                .thenReturn(new GetItemDto());

        itemService.findItemById(ownerId, itemId);

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemId(itemId);
        verify(commentStorage, times(1)).findAllByItemId(itemId);
        verify(itemMapper, times(1)).toGetItemDto(eq(item), any(), any());
        verify(bookingMapper, times(2)).toShortDto(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        assertThat(bookings.get(1), is(booking3));
        verify(commentMapper, times(1)).toDtoList(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещи по id, запрос не от владельца")
    void findItemById_WhenRequesterIsNotOwner_ShouldReturnItemWithoutBookingDates() {
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemId(itemId))
                .thenReturn(List.of(booking1, booking2, booking3));
        Comment comment = new Comment();
        when(commentStorage.findAllByItemId(itemId))
                .thenReturn(List.of(comment));
        GetItemDto getItemDto = new GetItemDto();
        when(itemMapper.toWithBookingsDto(item))
                .thenReturn(getItemDto);


        itemService.findItemById(requesterId, itemId);

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemId(itemId);
        verify(itemMapper, times(1)).toWithBookingsDto(item);
        verify(commentStorage, times(1)).findAllByItemId(itemId);
        verify(commentMapper, times(1)).toDtoList(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещи по id, пользователь не найден")
    void findItemById_WhenUserNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.empty());


        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.findItemById(requesterId, itemId));
        assertThat(e.getMessage(), is("Пользователь с id '" + requesterId + "' не найден."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, never()).findById(any());
        verify(bookingStorage, never()).findAllByItemId(any());
        verify(itemMapper, never()).toWithBookingsDto(any());
        verify(commentStorage, never()).findAllByItemId(any());
        verify(commentMapper, never()).toDtoList(any());
    }

    @Test
    @DisplayName("Поиск вещи по id, вещб не найдена")
    void findItemById_WhenItemNotFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.empty());


        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.findItemById(requesterId, itemId));
        assertThat(e.getMessage(), is("Вещь с id '" + itemId + "' не найдена."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, never()).findAllByItemId(any());
        verify(itemMapper, never()).toWithBookingsDto(any());
        verify(commentStorage, never()).findAllByItemId(any());
        verify(commentMapper, never()).toDtoList(any());
    }

    @Test
    @DisplayName("Поиск вещей пользователя")
    void findAllItemsByUserId_ShouldReturnItemsWithBookingAndComments() {
        long from = 0;
        int size = 4;
        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setStatus(BookingStatus.APPROVED);
        booking3.setStatus(BookingStatus.APPROVED);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        when(commentMapper.toDtoList(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        ShortBookingDto shortBookingDto = new ShortBookingDto();
        when(bookingMapper.toShortDto(any()))
                .thenReturn(shortBookingDto);
        when(itemMapper.toGetItemDto(eq(item), any(), any()))
                .thenReturn(new GetItemDto());

        List<GetItemDto> items = itemService.findAllItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toShortDto(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        assertThat(bookings.get(1), is(booking3));
        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toDtoList(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, следующее бронирование не подтверждено")
    void findAllItemsByUserId_WhenNextBookingsIsNotApproved_ShouldReturnItemsWithBookingAndComments() {
        long from = 0;
        int size = 4;
        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setStatus(BookingStatus.APPROVED);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        when(commentMapper.toDtoList(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        ShortBookingDto shortBookingDto = new ShortBookingDto();
        when(bookingMapper.toShortDto(any()))
                .thenReturn(shortBookingDto);
        when(itemMapper.toGetItemDto(eq(item), any(), any()))
                .thenReturn(new GetItemDto());

        List<GetItemDto> items = itemService.findAllItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toShortDto(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), is(booking1));
        assertThat(bookings.get(1), nullValue());
        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toDtoList(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, все бронирования не подтверждены")
    void findAllItemsByUserId_WhenBookingsAreNotApproved_ShouldReturnItemsWithNoBookingAndComments() {
        long from = 1;
        int size = 4;
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(booking1, booking2, booking3));
        LocalDateTime now = LocalDateTime.now();
        Comment comment = Comment.builder()
                .item(item)
                .text("comment")
                .created(now)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .text("dto comment")
                .authorName("author name")
                .build();
        when(commentMapper.toDtoList(List.of(comment)))
                .thenReturn(List.of(commentDto));
        when(commentStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(List.of(comment));
        ShortBookingDto shortBookingDto = new ShortBookingDto();
        when(bookingMapper.toShortDto(any()))
                .thenReturn(shortBookingDto);
        when(itemMapper.toGetItemDto(eq(item), any(), any()))
                .thenReturn(new GetItemDto());

        List<GetItemDto> items = itemService.findAllItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), is(List.of(commentDto)));
        verify(bookingMapper, times(2)).toShortDto(bookingArgumentCaptor.capture());
        List<Booking> bookings = bookingArgumentCaptor.getAllValues();
        assertThat(bookings.size(), is(2));
        assertThat(bookings.get(0), nullValue());
        assertThat(bookings.get(1), nullValue());
        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentMapper, times(1)).toDtoList(List.of(comment));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, бронирований нет")
    void findAllItemsByUserId_WhenNoBookings_ShouldReturnItemsWithoutBookingAndComments() {
        long from = 1;
        int size = 4;
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findAllByOwnerIdOrderById(eq(requesterId), any()))
                .thenReturn(List.of(item));
        when(bookingStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(Collections.emptyList());
        when(commentStorage.findAllByItemIdIn(List.of(itemId)))
                .thenReturn(Collections.emptyList());
        GetItemDto getItemDto = new GetItemDto();
        when(itemMapper.toWithBookingsDtoList(List.of(item)))
                .thenReturn(List.of(getItemDto));

        List<GetItemDto> items = itemService.findAllItemsByUserId(requesterId, from, size);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getComments(), empty());
        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findAllByOwnerIdOrderById(eq(requesterId),
                offsetPageRequestArgumentCaptor.capture());
        OffsetPageRequest captorValue = offsetPageRequestArgumentCaptor.getValue();
        assertThat(captorValue.getOffset(), is(from));
        assertThat(captorValue.getPageSize(), is(size));
        verify(bookingStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(commentStorage, times(1)).findAllByItemIdIn(List.of(itemId));
        verify(itemMapper, times(1)).toWithBookingsDtoList(List.of(item));
    }

    @Test
    @DisplayName("Поиск вещей пользователя, пользователь не найден")
    void findAllItemsByUserId_WhenUserNotFound_ShouldThrowNotFoundException() {
        long from = 1;
        int size = 4;
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.findAllItemsByUserId(requesterId, from, size));
        assertThat(e.getMessage(), is("Пользователь с id '" + requesterId + "' не найден."));
        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, never()).findAllByOwnerIdOrderById(any(), any());
        verify(bookingStorage, never()).findAllByItemIdIn(any());
        verify(commentStorage, never()).findAllByItemIdIn(any());
        verify(itemMapper, never()).toWithBookingsDtoList(any());
    }

    @Test
    @DisplayName("Поиск вещей пользователя")
    void searchItems_WhenTextIsNotBlank_ShouldReturnListOfItems() {
        long from = 1;
        int size = 4;
        String text = "search";
        when(itemStorage.searchInTitleAndDescription(any(), any()))
                .thenReturn(List.of(item));

        itemService.searchItems(text, from, size);

        verify(itemStorage, times(1)).searchInTitleAndDescription(stringArgumentCaptor.capture(),
                offsetPageRequestArgumentCaptor.capture());
        String captorValue = stringArgumentCaptor.getValue();
        assertThat(captorValue, is("%search%"));
        OffsetPageRequest offsetPageRequest = offsetPageRequestArgumentCaptor.getValue();
        assertThat(offsetPageRequest.getOffset(), is(from));
        assertThat(offsetPageRequest.getPageSize(), is(size));
        verify(itemMapper, times(1)).toDtoList(any());
    }

    @Test
    @DisplayName("Поиск вещей, верхний регистр")
    void searchItems_WhenTextUpperCase_ShouldReturnListOfItems() {
        long from = 1;
        int size = 4;
        String text = "SEArcH";
        when(itemStorage.searchInTitleAndDescription(any(), any()))
                .thenReturn(List.of(item));

        itemService.searchItems(text, from, size);

        verify(itemStorage, times(1)).searchInTitleAndDescription(stringArgumentCaptor.capture(),
                offsetPageRequestArgumentCaptor.capture());
        String captorValue = stringArgumentCaptor.getValue();
        assertThat(captorValue, is("%search%"));
        OffsetPageRequest offsetPageRequest = offsetPageRequestArgumentCaptor.getValue();
        assertThat(offsetPageRequest.getOffset(), is(from));
        assertThat(offsetPageRequest.getPageSize(), is(size));
        verify(itemMapper, times(1)).toDtoList(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи")
    void addCommentToItem_WhenUserIsAbleToAddComments_ShouldReturnCommentDto() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        booking1.setStatus(BookingStatus.APPROVED);
        booking2.setStatus(BookingStatus.APPROVED);
        booking3.setStatus(BookingStatus.APPROVED);
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemIdAndBookerId(itemId, requesterId))
                .thenReturn(List.of(booking1, booking2, booking3));
        when(commentStorage.save(any()))
                .thenReturn(new Comment());

        itemService.addCommentToItem(requesterId, itemId, addCommentDto);

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemIdAndBookerId(itemId, requesterId);
        verify(commentStorage, times(1)).save(commentArgumentCaptor.capture());
        Comment captorValue = commentArgumentCaptor.getValue();
        assertThat(captorValue.getText(), is(addCommentDto.getText()));
        assertThat(captorValue.getItem(), is(item));
        assertThat(captorValue.getAuthor(), is(requester));
        assertThat(captorValue.getCreated(), lessThanOrEqualTo(LocalDateTime.now()));
        verify(commentMapper, times(1)).toDto(any());
    }


    @Test
    @DisplayName("Добавление отзыва о вещи, от пользователя не бравшего вещь в аренду")
    void addCommentToItem_WhenUserUnableToAddComments_ShouldThrowItemUnavailableException() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemIdAndBookerId(itemId, ownerId))
                .thenReturn(List.of(booking1, booking2, booking3));

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> itemService.addCommentToItem(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + ownerId + "' не брал в аренду вещь с id '" +
                itemId + "'."));

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemIdAndBookerId(itemId, ownerId);
        verify(commentStorage, never()).save(any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, бронирование не подтверждено")
    void addCommentToItem_WhenUserIsAbleToAddCommentsButBookingsNotApproved_ShouldThrowItemUnavailableException() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemIdAndBookerId(itemId, requesterId))
                .thenReturn(List.of(booking1, booking2, booking3));

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> itemService.addCommentToItem(requesterId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + requesterId + "' не брал в аренду вещь с id '" +
                itemId + "'."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemIdAndBookerId(itemId, requesterId);
        verify(commentStorage, never()).save(any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, бронирование не закончилось")
    void addCommentToItem_WhenUserIsAbleToAddCommentsButBookingsHaveNotEnded_ShouldThrowItemUnavailableException() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        when(userStorage.findById(requesterId))
                .thenReturn(Optional.of(requester));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.of(item));
        when(bookingStorage.findAllByItemIdAndBookerId(itemId, requesterId))
                .thenReturn(List.of(booking2, booking3));

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> itemService.addCommentToItem(requesterId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + requesterId + "' не брал в аренду вещь с id '" +
                itemId + "'."));

        verify(userStorage, times(1)).findById(requesterId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, times(1)).findAllByItemIdAndBookerId(itemId, requesterId);
        verify(commentStorage, never()).save(any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, пользователь не найден")
    void addCommentToItem_WhenUserNotFound_ShouldThrowItemUnavailableException() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addCommentToItem(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + ownerId + "' не найден."));

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, never()).findById(any());
        verify(bookingStorage, never()).findAllByItemIdAndBookerId(any(), any());
        verify(commentStorage, never()).save(any());
        verify(commentMapper, never()).toDto(any());
    }

    @Test
    @DisplayName("Добавление отзыва о вещи, вещь не найдена")
    void addCommentToItem_WhenItemNotFound_ShouldThrowItemUnavailableException() {
        AddCommentDto addCommentDto = new AddCommentDto("new comment");
        when(userStorage.findById(ownerId))
                .thenReturn(Optional.of(owner));
        when(itemStorage.findById(itemId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addCommentToItem(ownerId, itemId, addCommentDto));
        assertThat(e.getMessage(), is("Вещь с id '" + itemId + "' не найдена."));

        verify(userStorage, times(1)).findById(ownerId);
        verify(itemStorage, times(1)).findById(itemId);
        verify(bookingStorage, never()).findAllByItemIdAndBookerId(any(), any());
        verify(commentStorage, never()).save(any());
        verify(commentMapper, never()).toDto(any());
    }
}