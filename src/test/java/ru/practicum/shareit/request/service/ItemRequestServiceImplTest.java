package ru.practicum.shareit.request.service;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.shared.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    @Test
    public void addNewItemRequest_ShouldSetRequester() {
        long userId = 1;
        User user = new User();
        String description = "description";
        AddItemRequestDto addItemRequestDto = new AddItemRequestDto(description);
        ItemRequest itemRequest = ItemRequest.builder().description(description).build();
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
        long userId = 1;
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
        long userId = 1;
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findAllByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(Collections.emptyList());
        when(itemRequestMapper.toDtoList(Collections.emptyList()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(Collections.emptyList()));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findAllByRequesterIdOrderByCreatedDesc(userId);
        verify(itemRequestMapper, times(1)).toDtoList(Collections.emptyList());
    }

    @Test
    public void getAllItemRequestsFromUser_ShouldReturnRequestList() {
        long userId = 1;
        ItemRequest itemRequest = new ItemRequest();
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        when(userStorage.findById(userId))
                .thenReturn(Optional.of(new User()));
        when(itemRequestStorage.findAllByRequesterIdOrderByCreatedDesc(userId))
                .thenReturn(List.of(itemRequest));
        when(itemRequestMapper.toDtoList(List.of(itemRequest)))
                .thenReturn(List.of(itemRequestDto));

        List<ItemRequestDto> requests = itemRequestService.getAllItemRequestsFromUser(userId);

        assertThat(requests, is(notNullValue()));
        assertThat(requests, is(List.of(itemRequestDto)));
        assertThat(requests.size(), is(1));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, times(1)).findAllByRequesterIdOrderByCreatedDesc(userId);
        verify(itemRequestMapper, times(1)).toDtoList(List.of(itemRequest));
    }

    @Test
    public void getAllItemRequestsFromUser_NoUserFound_ShouldThrowNotFoundException() {
        long userId = 1;

        when(userStorage.findById(userId))
                .thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class,
                () -> itemRequestService.getAllItemRequestsFromUser(userId));

        assertThat(e.getMessage(), is("Пользователь с id '1' не найден."));

        verify(userStorage, times(1)).findById(userId);
        verify(itemRequestStorage, never()).findAllByRequesterIdOrderByCreatedDesc(userId);
        verify(itemRequestMapper, never()).toDtoList(any());
    }

}