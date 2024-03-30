package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemStorageTest {

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    private Item savedItem1;

    private Item savedItem2;

    private Item savedItem3;

    private User savedUser1;

    private User savedUser2;

    @BeforeAll
    void init() {
        User user1 = createUser(1);
        savedUser1 = userStorage.save(user1);
        User user2 = createUser(2);
        savedUser2 = userStorage.save(user2);

        Item item1 = createItem(1);
        item1.setOwner(savedUser1);
        savedItem1 = itemStorage.save(item1);
        Item item2 = createItem(2);
        item2.setAvailable(false);
        item2.setOwner(savedUser1);
        savedItem2 = itemStorage.save(item2);
        Item item3 = createItem(3);
        item3.setOwner(savedUser2);
        savedItem3 = itemStorage.save(item3);
    }

    @AfterAll
    public void cleanDb() {
        itemStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    @DisplayName("Поиск вещей владельца")
    void findAllByOwnerIdOrderById_ShouldReturnListOfTwoItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).getId(), is(savedItem1.getId()));
        assertThat(items.get(1).getId(), is(savedItem2.getId()));
    }

    @Test
    @DisplayName("Поиск вещей владельца, начиная с второго элемента")
    void findAllByOwnerIdOrderById_When_OffsetIs1_ShouldReturnListOfTOneItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(1L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem2.getId()));
    }

    @Test
    @DisplayName("Поиск вещей владельца, начиная с первого элемента, количество элементов на странице 1")
    void findAllByOwnerIdOrderById_When_SizeIs1_ShouldReturnListOfTOneItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 1);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Поиск вещей неизвестного пользователя")
    void findAllByOwnerIdOrderById_WhenUnknownUser_ShouldReturnEmptyList() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(999L, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    @DisplayName("Поиск по названию и описанию вещи")
    void searchInTitleAndDescription_WhenSearchInTitle_ShouldReturnAllItems() {
        String text = "%name%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).getId(), is(savedItem1.getId()));
        assertThat(items.get(1).getId(), is(savedItem3.getId()));
    }

    @Test
    @DisplayName("Поиск по названию и описанию вещи, начиная со второго элемента")
    void searchInTitleAndDescription_WhenSearchInTitleAndFrom1_ShouldReturnAllItems() {
        String text = "%name%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(1L, 5);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem3.getId()));
    }

    @Test
    @DisplayName("Поиск по названию и описанию вещи, элементов на странице 1")
    void searchInTitleAndDescription_WhenSearchInTitleAndSize1_ShouldReturnAllItems() {
        String text = "%name%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 1);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem1.getId()));
    }

    @Test
    @DisplayName("Поиск по названию вещи")
    void searchInTitleAndDescription_WhenSearchInTitle2_ShouldReturnAllItems() {
        String text = "%name 3%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem3.getId()));
    }

    @Test
    @DisplayName("Поиск по описанию вещи")
    void searchInTitleAndDescription_WhenSearchInDescription_ShouldReturnAllItems() {
        String text = "%description%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items.get(0).getId(), is(savedItem1.getId()));
        assertThat(items.get(1).getId(), is(savedItem3.getId()));
    }

    @Test
    @DisplayName("Поиск по описанию вещи 3")
    void searchInTitleAndDescription_WhenSearchInDescription2_ShouldReturnAllItems() {
        String text = "%description 3%";
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);

        List<Item> items = itemStorage.searchInTitleAndDescription(text, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items.get(0).getId(), is(savedItem3.getId()));
    }

    private Item createItem(int id) {
        return Item.builder()
                .name("item name " + id)
                .description("item description " + id)
                .available(true)
                .build();
    }

    private User createUser(int id) {
        return User.builder()
                .name("user" + id)
                .email("requester" + id + "@email.com")
                .build();
    }
}