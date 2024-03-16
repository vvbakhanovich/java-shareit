package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeAll;
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

    @Test
    void addBooking_ShouldReturnBookingDtoWithIdNotNull() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        assertThat(addedBooking.getId(), notNullValue());
        assertThat(addedBooking.getItem().getId(), is(savedItem1.getId()));
        assertThat(addedBooking.getStart(), notNullValue());
        assertThat(addedBooking.getEnd(), notNullValue());
        assertThat(addedBooking.getStatus(), is(BookingStatus.WAITING));
    }

    @Test
    void addBooking_WhenOwnerTryToBookHisOwnItem_ShouldThrowNotAuthorizedException() {
        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.addBooking(owner.getId(), addBookingDto1));
        assertThat(e.getMessage(), is("Вещь с id '" + savedItem1.getId() +
                "' уже принадлежит пользователю с id '" + owner.getId() + "'."));
    }

    @Test
    void addBooking_WhenUserNotExists_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.addBooking(999L, addBookingDto1));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void acknowledgeBooking_WhenApproved_ShouldReturnBookingDtoWithApprovedStatus() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto bookingDto = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(BookingStatus.APPROVED));
    }

    @Test
    void acknowledgeBooking_WhenRejected_ShouldReturnBookingDtoWithRejectedStatus() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto bookingDto = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        assertThat(bookingDto, notNullValue());
        assertThat(bookingDto.getStatus(), is(BookingStatus.REJECTED));

    }

    @Test
    void acknowledgeBooking_WhenNotOwnerTryToApprove_ShouldThrowNotAuthorizedException () {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotAuthorizedException e = assertThrows(NotAuthorizedException.class,
                () -> bookingService.acknowledgeBooking(booker.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Пользователь с id '" + booker.getId() + "' не является владельцем вещи с id '"
                + savedItem1.getId() + "'."));
    }

    @Test
    void acknowledgeBooking_WhenBookingStatusIsNotWaiting_ShouldThrowItemUnavailableException () {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true);

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Текущий статус бронирования не позволяет сделать подтверждение."));
    }

    @Test
    void acknowledgeBooking_WhenUserNotFound_ShouldThrowNotFoundException () {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(999L, addedBooking.getId(), true));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void acknowledgeBooking_WhenBookingNotFound_ShouldThrowNotFoundException () {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.acknowledgeBooking(owner.getId(), 999L, true));
        assertThat(e.getMessage(), is("Бронирование с id '999' не найдено."));
    }

    @Test
    void getBookingById_WhenRequestFromOwner_ShouldReturnBookingDto() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto booking = bookingService.getBookingById(owner.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(BookingStatus.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
    void getBookingById_WhenRequestFromBooker_ShouldReturnBookingDto() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        BookingDto booking = bookingService.getBookingById(booker.getId(), addedBooking.getId());

        assertThat(booking, notNullValue());
        assertThat(booking.getStatus(), is(BookingStatus.WAITING));
        assertThat(booking.getBooker().getId(), is(booker.getId()));
        assertThat(booking.getItem().getId(), is(savedItem1.getId()));
    }

    @Test
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
    void getBookingById_WhenUserNotFound_ShouldThrowNotFoundException() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(999L, addedBooking.getId()));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateAllFromAndSizeAreNull_ShouldReturnAllBooking() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto acknowledgedBooking = bookingService.acknowledgeBooking(owner.getId(), addedBooking2.getId(),
                true);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.ALL, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(acknowledgedBooking, addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateAllFrom1Size1_ShouldReturnAllBooking() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.ALL, 1L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsBookerStateAllFromAndSizeAreNull_ShouldReturnAllBooking() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.ALL, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateCurrentFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.CURRENT, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.CURRENT, 0L,
                1, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsBookerStateCurrentFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.CURRENT, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsBookerStateCurrentFromAnd0Are1_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.CURRENT, 0L,
                1, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
    void getAllBookingsFromUser_WhenRequesterIsOwnerStatePastFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.PAST, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsBookerStatePastFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.PAST, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking3)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateFutureFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.FUTURE, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsBookerStateFutureFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.FUTURE, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking2)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateWaitingFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking2.getId(), true);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.WAITING, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsBookerStateWaitingFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        bookingService.acknowledgeBooking(owner.getId(), addedBooking2.getId(), true);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.WAITING, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(addedBooking)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsOwnerStateRejectedFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        BookingDto acknowledgedBooking = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(owner.getId(), GetBookingState.REJECTED, null,
                null, true);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(acknowledgedBooking)));
    }

    @Test
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
    void getAllBookingsFromUser_WhenRequesterIsBookerStateRejectedFromAndSizeAreNull_ShouldReturnAllCurrentBookings() {
        BookingDto addedBooking = bookingService.addBooking(booker.getId(), addBookingDto1);
        BookingDto addedBooking2 = bookingService.addBooking(booker.getId(), addBookingDto2);
        BookingDto addedBooking3 = bookingService.addBooking(owner.getId(), addBookingDto3);
        BookingDto acknowledgedBooking = bookingService.acknowledgeBooking(owner.getId(), addedBooking.getId(), false);

        List<BookingDto> bookings = bookingService.getAllBookingsFromUser(booker.getId(), GetBookingState.REJECTED, null,
                null, false);

        assertThat(bookings, notNullValue());
        assertThat(bookings, is(List.of(acknowledgedBooking)));
    }

    @Test
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