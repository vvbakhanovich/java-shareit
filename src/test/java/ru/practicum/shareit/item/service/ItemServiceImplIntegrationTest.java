package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.shared.exception.ItemUnavailableException;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private UserStorage userStorage;

    @Autowired
    private BookingService bookingService;

    private User savedUser1;

    private User savedUser2;

    private ItemDto itemDto;

    private BookingDto savedBooking1;
    private BookingDto savedBooking2;
    private BookingDto savedBooking3;

    @BeforeAll
    void init() {
        User user1 = User.builder()
                .name("user1")
                .email("user1@email.com")
                .build();
        savedUser1 = userStorage.save(user1);
        User user2 = User.builder()
                .name("user2")
                .email("user2@email.com")
                .build();
        savedUser2 = userStorage.save(user2);
        itemDto = ItemDto.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(true)
                .build();
    }

    @AfterAll
    public void deleteUsers() {
        userStorage.deleteAll();
    }

    @Test
    void addItem_ShouldReturnItemWithNotNullId() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        assertThat(savedItem, notNullValue());
        assertThat(savedItem.getId(), greaterThan(0L));
        assertThat(savedItem.getName(), is(itemDto.getName()));
        assertThat(savedItem.getDescription(), is(itemDto.getDescription()));
        assertThat(savedItem.getAvailable(), is(itemDto.getAvailable()));
    }

    @Test
    void addItem_UserNotExists_ShouldThrowNotFoundException() {
        ItemDto itemDto = ItemDto.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(true)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.addItem(999L, itemDto));

        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void updateItem_WhenAllUpdateFieldsNotNull_ShouldUpdateNameDescriptionAndAvailable() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    void updateItem_WhenAllUpdateDescriptionAndAvailable_ShouldUpdateDescriptionAndAvailable() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .description("new description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(savedItem.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    void updateItem_WhenAllUpdateNameAndDescritpion_ShouldUpdateNameDescription() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .description("new description")
                .build();

        ItemDto updatedItem = itemService.updateItem(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(itemUpdateDto.getDescription()));
        assertThat(updatedItem.getAvailable(), is(savedItem.getAvailable()));
    }

    @Test
    void updateItem_WhenAllUpdateNameAndAvailable_ShouldUpdateNameAndAvailable() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(savedUser1.getId(), savedItem.getId(), itemUpdateDto);

        assertThat(updatedItem, notNullValue());
        assertThat(updatedItem.getName(), is(itemUpdateDto.getName()));
        assertThat(updatedItem.getDescription(), is(savedItem.getDescription()));
        assertThat(updatedItem.getAvailable(), is(itemUpdateDto.getAvailable()));
    }

    @Test
    void updateItem_WhenUserIsNotOwner_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(savedUser2.getId(), savedItem.getId(), itemUpdateDto));
        assertThat(e.getMessage(), is("У пользователя с id '" + savedUser2.getId() + "' не найдена вещь с id '" +
                savedItem.getId() + "'."));
    }

    @Test
    void updateItem_WhenUserINotFound_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(999L, savedItem.getId(), itemUpdateDto));
        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void updateItem_WhenItemINotFound_ShouldThrowNotFoundException() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("new name")
                .available(false)
                .build();

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemService.updateItem(savedUser1.getId(), 999L, itemUpdateDto));
        assertThat(e.getMessage(), is("Вещь с id '999' не найдена."));
    }

    @Test
    void findItemById_WhenRequestByOwner_ShouldReturnItemWithBookings() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);


        GetItemDto item = itemService.findItemById(savedUser1.getId(), itemId);

        assertThat(item, notNullValue());
        assertThat(item.getComments(), emptyIterable());
        assertThat(item.getLastBooking().getStart(), is(savedBooking1.getStart()));
        assertThat(item.getLastBooking().getEnd(), is(savedBooking1.getEnd()));
        assertThat(item.getLastBooking().getBookerId(), is(savedUser2.getId()));
        assertThat(item.getNextBooking().getStart(), is(savedBooking3.getStart()));
        assertThat(item.getNextBooking().getEnd(), is(savedBooking3.getEnd()));
        assertThat(item.getNextBooking().getBookerId(), is(savedUser2.getId()));
    }

    @Test
    void findItemById_WhenRequestByOtherUser_ShouldReturnItemWithoutBookings() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);


        GetItemDto item = itemService.findItemById(savedUser2.getId(), itemId);

        assertThat(item, notNullValue());
        assertThat(item.getComments(), emptyIterable());
        assertThat(item.getLastBooking(), nullValue());
        assertThat(item.getNextBooking(), nullValue());
    }

    @Test
    void findAllItemsByUserId_ShouldReturnOwnersItemListWithBookings() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);
        long itemId = savedItem.getId();
        setUpBookings(itemId);

        List<GetItemDto> items = itemService.findAllItemsByUserId(savedUser1.getId());

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getLastBooking().getStart(), is(savedBooking1.getStart()));
        assertThat(items.get(0).getLastBooking().getEnd(), is(savedBooking1.getEnd()));
        assertThat(items.get(0).getLastBooking().getBookerId(), is(savedUser2.getId()));
        assertThat(items.get(0).getNextBooking().getStart(), is(savedBooking3.getStart()));
        assertThat(items.get(0).getNextBooking().getEnd(), is(savedBooking3.getEnd()));
        assertThat(items.get(0).getNextBooking().getBookerId(), is(savedUser2.getId()));
    }

    @Test
    void findAllItemsByUserId_WhenUserNotHaveItems_ShouldReturnEmptyList() {

        List<GetItemDto> items = itemService.findAllItemsByUserId(savedUser1.getId());

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    void searchItems_ShouldReturnItemsContainingTextInTitleOrDescription() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        List<ItemDto> items = itemService.searchItems("Dto");

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    void searchItems_TextUpperCase_ShouldReturnItemsContainingTextInTitleOrDescription() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        List<ItemDto> items = itemService.searchItems("DTO");

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    void searchItems_SearchOnlyDescription_ShouldReturnItemsContainingTextInTitleOrDescription() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);

        List<ItemDto> items = itemService.searchItems("DEScripTioN");

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getName(), is(itemDto.getName()));
        assertThat(items.get(0).getDescription(), is(itemDto.getDescription()));
    }

    @Test
    void searchItems_WhenItemUnavailable_ShouldReturnEmptyList() {
        ItemDto unavailableItemDto = ItemDto.builder()
                .name("itemDto")
                .description("itemDto description")
                .available(false)
                .build();
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), unavailableItemDto);

        List<ItemDto> items = itemService.searchItems("DEScripTioN");

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    void addCommentToItem_ShouldReturnCommentWithNotNullId() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);
        setUpBookings(savedItem.getId());
        AddCommentDto addCommentDto = new AddCommentDto("comment");

        CommentDto commentDto = itemService.addCommentToItem(savedUser2.getId(), savedItem.getId(), addCommentDto);

        assertThat(commentDto, notNullValue());
        assertThat(commentDto.getAuthorName(), is(savedUser2.getName()));
        assertThat(commentDto.getText(), is(addCommentDto.getText()));
        assertThat(commentDto.getCreated(), lessThan(LocalDateTime.now()));
    }

    @Test
    void addCommentToItem_WhenUserThatNotBookedItem_ShouldThrowItemUnavailableException() {
        ItemDto savedItem = itemService.addItem(savedUser1.getId(), itemDto);
        setUpBookings(savedItem.getId());
        AddCommentDto addCommentDto = new AddCommentDto("comment");

        ItemUnavailableException e = assertThrows(ItemUnavailableException.class,
                () -> itemService.addCommentToItem(savedUser1.getId(), savedItem.getId(), addCommentDto));
        assertThat(e.getMessage(), is("Пользователь с id '" + savedUser1.getId() + "' не брал в аренду вещь с id '" +
                savedItem.getId() + "'."));
    }


    private void setUpBookings(long itemId) {
        AddBookingDto addBookingDto1 = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(4))
                .build();
        savedBooking1 = bookingService.addBooking(savedUser2.getId(), addBookingDto1);
        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking1.getId(), true);
        AddBookingDto addBookingDto2 = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().minusDays(5))
                .end(LocalDateTime.now().minusDays(4))
                .build();
        savedBooking2 = bookingService.addBooking(savedUser2.getId(), addBookingDto2);
        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking2.getId(), true);
        AddBookingDto addBookingDto3 = AddBookingDto.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(5))
                .build();
        savedBooking3 = bookingService.addBooking(savedUser2.getId(), addBookingDto3);
        bookingService.acknowledgeBooking(savedUser1.getId(), savedBooking3.getId(), true);
    }
}