package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.shared.exception.ItemUnavailableException;
import ru.practicum.shareit.shared.exception.NotAuthorizedException;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    private User owner;

    private User booker;

    private Item savedItem1;

    private Item savedItem2;

    private AddBookingDto addBookingDto1;

    private AddBookingDto addBookingDto2;

    private AddBookingDto addBookingDto3;

    @BeforeAll
    void init() {
        User user1 = User.builder()
                .name("owner")
                .email("owner@mail.com")
                .build();
        owner = userStorage.save(user1);

        User user2 = User.builder()
                .name("booker")
                .email("booker@mail.com")
                .build();
        booker = userStorage.save(user2);

        Item item1 = Item.builder()
                .name("itemName")
                .description("itemDescription")
                .available(true)
                .owner(owner)
                .build();
        savedItem1 = itemStorage.save(item1);

        Item item2 = Item.builder()
                .name("itemName2")
                .description("itemDescription2")
                .available(true)
                .owner(booker)
                .build();
        savedItem2 = itemStorage.save(item2);

        addBookingDto1 = AddBookingDto.builder()
                .itemId(savedItem1.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        addBookingDto2 = AddBookingDto.builder()
                .itemId(savedItem1.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();

        addBookingDto3 = AddBookingDto.builder()
                .itemId(savedItem2.getId())
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .build();
    }

    @AfterAll
    public void cleanDb() {
        userStorage.deleteAll();
        itemStorage.deleteAll();
    }

    @Test
    @DisplayName("Добавление бронирования")
    void addBooking_ShouldReturnBookingDtoWithIdNotNull() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        assertThat(addedBooking.getId(), notNullValue());
        assertThat(addedBooking.getItem().getId(), is(savedItem1.getId()));
        assertThat(addedBooking.getStart(), notNullValue());
        assertThat(addedBooking.getEnd(), notNullValue());
        assertThat(addedBooking.getStatus(), is(BookingStatus.WAITING));
    }

    @Test
    @DisplayName("Добавление бронирования на собственную вещь")
    void addBooking_WhenOwnerTryToBookHisOwnItem_ShouldThrowNotAuthorizedException() {
        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.addBooking(owner.getId(), addBookingDto1));
        assertThat(e.getMessage(), is("Вещь с id '" + savedItem1.getId() +
                "' уже принадлежит пользователю с id '" + owner.getId() + "'."));
    }

    @Test
    @DisplayName("Добавление бронирования, пользователь не найден")
    void addBooking_WhenUserNotExists_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(999L, addBookingDto1));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    @DisplayName("Подтверждение бронирования")
    void acknowledgeBooking_WhenApproved_ShouldReturnBookingDtoWithApprovedStatus() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto bookingDto = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    @DisplayName("Отмена бронирования")
    void acknowledgeBooking_WhenRejected_ShouldReturnBookingDtoWithRejectedStatus() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto bookingDto = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(BookingStatus.REJECTED));

    }

    @Test
    @DisplayName("Подтверждение бронирования не владельцем вещи")
    void acknowledgeBooking_WhenNotOwnerTryToApprove_ShouldThrowNotAuthorizedException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.acknowledgeBooking(booker.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Пользователь с id '" + booker.getId() + "' не является владельцем вещи с id '"
                + savedItem1.getId() + "'."));
    }

    @Test
    @DisplayName("Подтверждение бронирования с неверным статусом")
    void acknowledgeBooking_WhenBookingStatusIsNotWaiting_ShouldThrowItemUnavailableException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true);

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Текущий статус бронирования не позволяет сделать подтверждение."));
    }

    @Test
    @DisplayName("Подтверждение бронирования, пользователь не найден")
    void acknowledgeBooking_WhenUserNotFound_ShouldThrowNotFoundException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(999L, addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    @DisplayName("Подтверждение бронирования, бронирование не найдено")
    void acknowledgeBooking_WhenBookingNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(owner.getId(), 999L, true));
        assertThat(e.getMessage(), is("Бронирование с id '999' не найдено."));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос от владельца")
    void getBookingById_WhenRequestFromOwner_ShouldReturnBookingDto() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto booking = bookingService.getBookingById(owner.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(BookingStatus.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос пользователя, делающего бронирование")
    void getBookingById_WhenRequestFromBooker_ShouldReturnBookingDto() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto booking = bookingService.getBookingById(booker.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(BookingStatus.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Получение бронирования по id, запрос от другого пользователя")
    void getBookingById_WhenRequestFromAnotherUser_ShouldThrowNotAuthorizedException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        User user3 = User.builder()
                .name("anotherUser")
                .email("anotherUser@mail.com")
                .build();
        User anotherUser = userStorage.save(user3);

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.getBookingById(anotherUser.getId(), addedBooking.getId()));
        assertThat(e.getMessage(), is("У пользователя с id '" + anotherUser.getId() + "' нет прав для доступа к бронированию с" +
                " id '" + addedBooking.getId() + "'."));
    }

    @Test
    @DisplayName("Получение бронирования по id, пользователь не найден")
    void getBookingById_WhenUserNotFound_ShouldThrowNotFoundException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, addedBooking.getId()));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    @DisplayName("Получение всех бронирований от владельца, начиная с 1го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateAllFrom1Size1_ShouldReturnAllBooking() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.ALL, 1L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение всех бронирований, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStateAllFrom0Size1_ShouldReturnAllBooking() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.ALL, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение текущих бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.CURRENT, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение текущих бронирований, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.CURRENT, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение прошедших бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStatePastFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.PAST, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
    @DisplayName("Получение прошедших бронирований, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStatePastFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.PAST, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
    @DisplayName("Получение будущих бронирований от владельца, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateFutureFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.FUTURE, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение будущих бронирований, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStateFutureFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.FUTURE, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING от владельца, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateWaitingFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking2.getId(), true);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.WAITING, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом WAITING, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStateWaitingFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking2.getId(), true);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.WAITING, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED от владельца, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateRejectedFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        BookingDto acknowledgedBooking = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.REJECTED, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(acknowledgedBooking)));
    }

    @Test
    @DisplayName("Получение бронирований со статусом REJECTED, начиная с 0го элемента по 1 на странице")
    void getAllBookingsFromUser_WhenRequesterIsBookerStateRejectedFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        BookingDto acknowledgedBooking = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.REJECTED, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(acknowledgedBooking)));
    }
}