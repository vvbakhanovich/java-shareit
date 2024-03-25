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
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommentStorageTest {

    @Autowired
    private CommentStorage commentStorage;

    @Autowired
    private ItemStorage itemStorage;

    @Autowired
    private UserStorage userStorage;

    private Item savedItem1;

    private Item savedItem2;

    private Item savedItem3;

    private User savedUser1;

    private User savedUser2;

    private Comment savedComment1;

    private Comment savedComment2;

    private Comment savedComment3;

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

        Comment comment1 = createComment(1);
        comment1.setAuthor(savedUser2);
        comment1.setItem(savedItem1);
        savedComment1 = commentStorage.save(comment1);

        Comment comment2 = createComment(2);
        comment2.setAuthor(savedUser2);
        comment2.setItem(savedItem2);
        savedComment2 = commentStorage.save(comment2);

        Comment comment3 = createComment(4);
        comment3.setAuthor(savedUser2);
        comment3.setItem(savedItem2);
        savedComment3 = commentStorage.save(comment3);
    }

    @AfterAll
    public void cleanDb() {
        commentStorage.deleteAll();
        itemStorage.deleteAll();
        userStorage.deleteAll();
    }

    @Test
    @DisplayName("Поиск комментариев по id вещи")
    void findAllByItemId_ShouldReturnAllCommentsOnItem2() {
        List<Comment> comments = commentStorage.findAllByItemId(savedItem2.getId());

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(2));
        assertThat(comments.get(0).getId(), is(savedComment2.getId()));
        assertThat(comments.get(0).getText(), is(savedComment2.getText()));
        assertThat(comments.get(1).getId(), is(savedComment3.getId()));
        assertThat(comments.get(1).getText(), is(savedComment3.getText()));
    }

    @Test
    @DisplayName("Поиск комментариев по id вещи, когда нет оставленных комментариев")
    void findAllByItemId_WhenItemWithoutComments_ShouldReturnEmptyList() {
        List<Comment> comments = commentStorage.findAllByItemId(savedItem3.getId());

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Test
    @DisplayName("Поиск комментариев по id несуществующей вещи")
    void findAllByItemId_WhenUnknownItem_ShouldReturnEmptyList() {
        List<Comment> comments = commentStorage.findAllByItemId(999L);

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
    }

    @Test
    @DisplayName("Поиск комментариев по списку id вещей")
    void findAllByItemIdIn_ShouldReturnAllCommentsFromItem1AndItem2() {
        List<Comment> comments = commentStorage.findAllByItemIdIn(List.of(savedItem1.getId(), savedItem2.getId()));

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(3));
        assertThat(comments.get(0).getId(), is(savedComment1.getId()));
        assertThat(comments.get(0).getText(), is(savedComment1.getText()));
        assertThat(comments.get(1).getId(), is(savedComment2.getId()));
        assertThat(comments.get(1).getText(), is(savedComment2.getText()));
        assertThat(comments.get(2).getId(), is(savedComment3.getId()));
        assertThat(comments.get(2).getText(), is(savedComment3.getText()));
    }

    @Test
    @DisplayName("Поиск комментариев по списку из id только savedItem2")
    void findAllByItemIdIn_OnlyFromItem2_ShouldReturnAllCommentsFromItem2() {
        List<Comment> comments = commentStorage.findAllByItemIdIn(List.of(savedItem2.getId()));

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(2));
        assertThat(comments.get(0).getId(), is(savedComment2.getId()));
        assertThat(comments.get(0).getText(), is(savedComment2.getText()));
        assertThat(comments.get(1).getId(), is(savedComment3.getId()));
        assertThat(comments.get(1).getText(), is(savedComment3.getText()));
    }

    @Test
    @DisplayName("Поиск комментариев по списку из id только savedItem3")
    void findAllByItemIdIn_OnlyFromItem3_ShouldReturnEmptyList() {
        List<Comment> comments = commentStorage.findAllByItemIdIn(List.of(savedItem3.getId()));

        assertThat(comments, notNullValue());
        assertThat(comments.size(), is(0));
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

    private Comment createComment(int id) {
        return Comment.builder()
                .text("comment " + id)
                .created(LocalDateTime.now())
                .build();
    }
}