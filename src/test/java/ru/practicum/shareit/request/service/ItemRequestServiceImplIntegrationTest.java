package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserStorage userStorage;

    private User savedUser;

    private AddItemRequestDto addItemRequestDto;

    @BeforeAll
    public void setUp() {
        User user = User.builder().name("username").email("test@email.com").build();
        savedUser = userStorage.save(user);
        addItemRequestDto = new AddItemRequestDto("description");
    }

    @Test
    void addNewItemRequest_ShouldReturnRequestDto() {
        ItemRequestDto itemRequestDto = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        assertThat(itemRequestDto, notNullValue());
        assertThat(itemRequestDto.getId(), greaterThan(0L));
        assertThat(itemRequestDto.getDescription(), is(addItemRequestDto.getDescription()));
        assertThat(itemRequestDto.getCreated(), notNullValue());
    }

    @Test
    void addNewItemRequest_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.addNewItemRequest(999L, addItemRequestDto));

        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void getAllItemRequestsFromUser_ShouldReturnRequestList() {
        ItemRequestDto itemRequestDto = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        List<ItemRequestDto> requests = itemRequestService.getAllItemRequestsFromUser(savedUser.getId());

        assertThat(requests, notNullValue());
        assertThat(requests, is(List.of(itemRequestDto)));
    }

    @Test
    void getAllItemRequestsFromUser_UserNotFound_ShouldThrowNotFoundException() {
        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsFromUser(999L));

        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void getAvailableItemRequests_From0Size5_ShouldReturn2Requests() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        AddItemRequestDto addItemRequestDto2 = new AddItemRequestDto("description 2");
        AddItemRequestDto addItemRequestDto3 = new AddItemRequestDto("description 3");
        User savedUser2 = userStorage.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDto savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        ItemRequestDto savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDto> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 0L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, is(List.of(savedRequest3, savedRequest2)));
    }

    @Test
    void getAvailableItemRequests_From1Size5_ShouldReturn1Requests() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        AddItemRequestDto addItemRequestDto2 = new AddItemRequestDto("description 2");
        AddItemRequestDto addItemRequestDto3 = new AddItemRequestDto("description 3");
        User savedUser2 = userStorage.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDto savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        ItemRequestDto savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDto> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 1L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, is(List.of(savedRequest2)));
    }

    @Test
    void getAvailableItemRequests_From2Size5_ShouldReturnEmptyList() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        AddItemRequestDto addItemRequestDto2 = new AddItemRequestDto("description 2");
        AddItemRequestDto addItemRequestDto3 = new AddItemRequestDto("description 3");
        User savedUser2 = userStorage.save(user2);
        itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDto savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        ItemRequestDto savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDto> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), 2L, 5);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, emptyIterable());
    }

    @Test
    void getAvailableItemRequests_FromNullSizeNull_ShouldReturnAllRequests() {
        User user2 = User.builder().name("username2").email("test2@email.com").build();
        AddItemRequestDto addItemRequestDto2 = new AddItemRequestDto("description 2");
        AddItemRequestDto addItemRequestDto3 = new AddItemRequestDto("description 3");
        User savedUser2 = userStorage.save(user2);
        ItemRequestDto savedRequest1 = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);
        ItemRequestDto savedRequest2 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto2);
        ItemRequestDto savedRequest3 = itemRequestService.addNewItemRequest(savedUser2.getId(), addItemRequestDto3);

        List<ItemRequestDto> availableItemRequests = itemRequestService
                .getAvailableItemRequests(savedUser.getId(), null, null);

        assertThat(availableItemRequests, notNullValue());
        assertThat(availableItemRequests, is(List.of(savedRequest3, savedRequest2, savedRequest1)));
    }

    @Test
    void getItemRequestById_ShouldReturnItem() {
        ItemRequestDto savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        ItemRequestDto request = itemRequestService.getItemRequestById(savedUser.getId(), savedRequest.getId());

        assertThat(request, notNullValue());
        assertThat(request, is(savedRequest));
    }

    @Test
    void getItemRequestById_UserNotFound_ShouldThrowNotFoundException() {
        ItemRequestDto savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(999L, savedRequest.getId()));

        assertThat(e.getMessage(), is("Пользователь с id '999' не найден."));
    }

    @Test
    void getItemRequestById_RequestNotFound_ShouldThrowNotFoundException() {
        ItemRequestDto savedRequest = itemRequestService.addNewItemRequest(savedUser.getId(), addItemRequestDto);

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(savedUser.getId(), 999L));

        assertThat(e.getMessage(), is("Запрос с id '999' не найден."));
    }
}