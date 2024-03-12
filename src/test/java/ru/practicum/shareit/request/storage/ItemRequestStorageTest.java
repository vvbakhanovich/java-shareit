package ru.practicum.shareit.request.storage;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemRequestStorageTest {

    @Autowired
    private ItemRequestStorage itemRequestStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    private User savedUser1;
    private User savedUser2;
    private Item savedItem;
    private ItemRequest savedRequest1;
    private ItemRequest savedRequest2;
    private ItemRequest savedRequest3;
    private ItemRequest savedRequest4;

    @BeforeAll
    public void init() {
        User user1 = User.builder().name("username").email("test@email.com").build();
        savedUser1 = userStorage.save(user1);
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        savedUser2 = userStorage.save(user2);


        ItemRequest itemRequest1 = ItemRequest.builder().requester(savedUser1).description("description1").build();
        savedRequest1 = itemRequestStorage.save(itemRequest1);
        Item item = Item.builder().owner(savedUser1).available(true).name("name").description("description")
                .request(savedRequest1).build();
        savedItem = itemStorage.save(item);
        savedRequest1.addItem(savedItem);
        savedRequest1 = itemRequestStorage.save(itemRequest1);
        ItemRequest itemRequest2 = ItemRequest.builder().requester(savedUser2).description("description2").build();
        savedRequest2 = itemRequestStorage.save(itemRequest2);
        ItemRequest itemRequest3 = ItemRequest.builder().requester(savedUser2).description("description3").build();
        savedRequest3 = itemRequestStorage.save(itemRequest3);
        ItemRequest itemRequest4 = ItemRequest.builder().requester(savedUser1).description("description4").build();
        savedRequest4 = itemRequestStorage.save(itemRequest4);
    }

    @Test
    public void findItemRequestsFromUser_ReturnRequestsWithoutItemsFromRequester() {
        List<ItemRequest> requests = itemRequestStorage.findRequestsFromUser(savedUser2.getId());

        assertThat(requests.size(), is(2));
        assertThat(requests.get(0).getId(), is(savedRequest3.getId()));
        assertThat(requests.get(1).getId(), is(savedRequest2.getId()));
        assertThat(requests.get(0).getCreated(), is(notNullValue()));
        assertThat(requests.get(1).getCreated(), is(notNullValue()));
        assertThat(requests.get(0).getItems().size(), is(0));
        assertThat(requests.get(1).getItems().size(), is(0));
    }

    @Test
    public void findItemRequestsFromUser_ReturnRequestsWithItemsFromRequester() {
        List<ItemRequest> requests = itemRequestStorage.findRequestsFromUser(savedUser1.getId());

        assertThat(requests.size(), is(2));
        assertThat(requests.get(0).getId(), is(savedRequest4.getId()));
        assertThat(requests.get(1).getId(), is(savedRequest1.getId()));
        assertThat(requests.get(0).getCreated(), is(notNullValue()));
        assertThat(requests.get(1).getCreated(), is(notNullValue()));
        assertThat(requests.get(0).getItems().size(), is(0));
        assertThat(requests.get(1).getItems().size(), is(1));
    }

    @Test
    @SneakyThrows
    public void findItemRequestsFromUser_ReturnRequestsWithItemsFromRequesterSortedByCreated() {
        List<ItemRequest> requests = itemRequestStorage.findRequestsFromUser(savedUser1.getId());

        assertThat(requests, is(List.of(savedRequest4, savedRequest1)));
        assertThat(requests.size(), is(2));
    }

    @Test
    public void findAllByOrderByCreatedDesc_From0Size1_ShouldReturnListOfItemRequest() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0, 1);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(),
                pageRequest);

        assertThat(requests.getContent(), is(List.of(savedRequest3)));
        assertThat(requests.getContent().size(), is(1));
    }

    @Test
    @SneakyThrows
    public void findAllByOrderByCreatedDesc_From0Size10_ShouldReturnListOfTwoItemRequest() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0, 10);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(),
                pageRequest);

        assertThat(requests.getContent(), is(List.of(savedRequest3, savedRequest2)));
        assertThat(requests.getContent().size(), is(2));
    }

    @Test
    public void findAllByOrderByCreatedDesc_From0Size0_ShouldReturnEmptyList() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0, 0);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(),
                pageRequest);

        assertThat(requests.getContent(), is(Collections.emptyList()));
        assertThat(requests.getContent().size(), is(0));
    }

    @Test
    public void findAllByOrderByCreatedDesc_From1Size1_ShouldReturnListOfItemRequest() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(1, 1);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(),
                pageRequest);

        assertThat(requests.getContent(), is(List.of(savedRequest2)));
        assertThat(requests.getContent().size(), is(1));
    }

    @Test
    public void findAllByOrderByCreatedDesc_From1Size1_ShouldReturnAllItemRequest() {
        OffsetPageRequest pageRequest = OffsetPageRequest.of(0, 2);

        Page<ItemRequest> requests = itemRequestStorage.findAllByRequesterIdNotOrderByCreatedDesc(savedUser1.getId(),
                pageRequest);

        assertThat(requests.getContent(), is(List.of(savedRequest3, savedRequest2)));
        assertThat(requests.getContent().size(), is(2));
    }
}