package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.shared.OffsetPageRequest;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestStorage itemRequestStorage;

    @Mock
    private UserStorage userStorage;

    @Mock
    private ItemRequestMapper itemRequestMapper;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    @Captor
    private ArgumentCaptor<ItemRequest> itemRequestArgumentCaptor;

    private long userId;

    private ItemRequest itemRequest;

    @BeforeEach
    public void init() {
        userId = 1;
        itemRequest = ItemRequest.builder().description("description").build();
    }

    @Test
    public void addNewItemRequest_ShouldSetRequester() {
        User user = new User();
        String description = "description";
        AddItemRequestDto addItemRequestDto = new AddItemRequestDto(description);
        ItemRequest itemRequest2 = ItemRequest.builder().description(description).build();

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(user));
        when(itemRequestMapper.toModel(addItemRequestDto))
                .thenReturn(itemRequest);
        when(itemRequestStorage.save(any()))
                .thenReturn(itemRequest2);

        itemRequestService.addNewItemRequest(userId, addItemRequestDto);

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestMapper, times(1)).toModel(addItemRequestDto);
        verify(itemRequestStorage, times(1)).save(itemRequestArgumentCaptor.capture());
        ItemRequest captorValue = itemRequestArgumentCaptor.getValue();

        assertThat(captorValue, is(notNullValue()));
        assertThat(captorValue.getRequester(), is(user));
    }

    @Test
    public void addNewItemRequest_NoUserFound_ThrowNotFoundException() {
        AddItemRequestDto addItemRequestDto = new AddItemRequestDto();
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.addNewItemRequest(userId, addItemRequestDto));
        assertThat(e.getMessage(), is("Пользователь с id '1' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestMapper, never()).toModel(any());
        verify(itemRequestStorage, never()).save(any());
    }

    @Test
    public void getAllItemRequestsFromUser_ShouldReturnEmptyList() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findRequestsFromUser(userId))
                .thenReturn(Collections.emptyList());
        when(itemRequestMapper.toDtoList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(Collections.emptyList()));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findRequestsFromUser(userId);
        verify(itemRequestMapper, times(1)).toDtoList(Collections.emptyList());
    }

    @Test
    public void getAllItemRequestsFromUser_ShouldReturnRequestList() {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findRequestsFromUser(userId))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(List.of(itemRequestDto)));
        assertThat(requests.size(), is(1));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findRequestsFromUser(userId);
        verify(itemRequestMapper, times(1)).toDtoList(List.of(itemRequest));
    }

    @Test
    public void getAllItemRequestsFromUser_NoUserFound_ShouldThrowNotFoundException() {
        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsFromUser(userId));

        assertThat(e.getMessage(), is("Пользователь с id '1' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, never()).findRequestsFromUser(userId);
        verify(itemRequestMapper, never()).toDtoList(any());
    }

    @Test
    public void getAvailableItemRequests_WithNullFromAndSize_ShouldInvokeFindAll() {
        Long from = null;
        Integer size = null;

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));

        itemRequestService.getAvailableItemRequests(userId, from, size);

        verify(itemRequestStorage, times(1)).findAllRequests();
        verify(itemRequestMapper, times(1)).toDtoList(any());
    }

    @Test
    public void getAvailableItemRequests_UserNotExists_ShouldThrowNotFoundException() {
        Long from = 1L;
        Integer size = 1;

        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Пользователь с id '1' не найден."));

        verify(itemRequestStorage, never()).findAllRequests();
        verify(itemRequestStorage, never()).findAvailableRequests(userId, OffsetPageRequest.of(from, size));
        verify(itemRequestMapper, never()).toDtoList(any());
    }

    @Test
    public void getAvailableItemRequests_FromNullSizeNotNull_ShouldThrowIllegalArgumentException() {
        Long from = null;
        Integer size = 1;

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Offset must be positive or zero!"));

        verify(itemRequestStorage, never()).findAllRequests();
        verify(itemRequestMapper, never()).toDtoList(any());
    }

    @Test
    public void getAvailableItemRequests_FromNotNullSizeNull_ShouldThrowIllegalArgumentException() {
        Long from = 1L;
        Integer size = null;

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> itemRequestService.getAvailableItemRequests(userId, from, size));

        assertThat(e.getMessage(), is("Page size must be positive!"));

        verify(itemRequestStorage, never()).findAllRequests();
        verify(itemRequestMapper, never()).toDtoList(any());
    }

    @Test
    public void getAvailableItemRequests_WithNotNullFromAndSize_ShouldInvokeFindAllPageable() {
        Long from = 1L;
        Integer size = 2;

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findAvailableRequests(eq(userId), any()))
                .thenReturn(Page.empty());

        itemRequestService.getAvailableItemRequests(userId, from, size);

        verify(itemRequestStorage, never()).findAllRequests();
        verify(itemRequestStorage, times(1)).findAvailableRequests(eq(userId),
                any());
        verify(itemRequestMapper, times(1)).toDtoList(any());
    }

    @Test
    public void getItemRequestById_UserNotFound_ShouldThrowNotFoundException() {
        long requestId = 1;

        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));

        assertThat(e.getMessage(), is("Пользователь с id '1' не найден."));

        verify(itemRequestMapper, never()).toDto(any());
    }

    @Test
    public void getItemRequestById_RequestNotFound_ShouldThrowNotFoundException() {
        long requestId = 1;

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getItemRequestById(userId, requestId));

        assertThat(e.getMessage(), is("Запрос с id '1' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestMapper, never()).toDto(any());
    }

    @Test
    public void getItemRequestById_ShouldReturnRequest() {
        long requestId = 1;
        itemRequest.setId(1L);

        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findById(requestId))
                .thenReturn(Optional.of(itemRequest));

        itemRequestService.getItemRequestById(userId, requestId);

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findById(requestId);
        verify(itemRequestMapper, times(1)).toDto(itemRequest);
    }
}