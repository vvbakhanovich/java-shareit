package ru.practicum.shareit.item.storage;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
    void findAllByOwnerIdOrderById_ShouldReturnListOfTwoItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items, is(List.of(savedItem1, savedItem2)));
    }

    @Test
    void findAllByOwnerIdOrderById_When_OffsetIs1_ShouldReturnListOfTOneItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(1L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items, is(List.of(savedItem2)));
    }

    @Test
    void findAllByOwnerIdOrderById_When_SizeIs1_ShouldReturnListOfTOneItems() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 1);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(savedUser1.getId(), pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items, is(List.of(savedItem1)));
    }

    @Test
    void findAllByOwnerIdOrderById_WhenUnknownUser_ShouldReturnEmptyList() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0L, 5);
        List<Item> items = itemStorage.findAllByOwnerIdOrderById(999L, pageRequest);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(0));
    }

    @Test
    void searchInTitleAndDescription_WhenSearchInTitle_ShouldReturnAllItems() {
        String text = "%name%";

        List<Item> items = itemStorage.searchInTitleAndDescription(text);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items, is(List.of(savedItem1, savedItem3)));
    }

    @Test
    void searchInTitleAndDescription_WhenSearchInTitle2_ShouldReturnAllItems() {
        String text = "%name 3%";

        List<Item> items = itemStorage.searchInTitleAndDescription(text);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items, is(List.of(savedItem3)));
    }

    @Test
    void searchInTitleAndDescription_WhenSearchInDescription_ShouldReturnAllItems() {
        String text = "%description%";

        List<Item> items = itemStorage.searchInTitleAndDescription(text);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items, is(List.of(savedItem1, savedItem3)));
    }

    @Test
    void searchInTitleAndDescription_WhenSearchInDescription2_ShouldReturnAllItems() {
        String text = "%description 3%";

        List<Item> items = itemStorage.searchInTitleAndDescription(text);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(1));
        assertThat(items, is(List.of(savedItem3)));
    }

    @Test
    void searchInTitleAndDescription_WhenSearchTextIsEmpty_ShouldReturnAllItems() {
        String text = "%%";

        List<Item> items = itemStorage.searchInTitleAndDescription(text);

        assertThat(items, notNullValue());
        assertThat(items.size(), is(2));
        assertThat(items, is(List.of(savedItem1, savedItem3)));
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