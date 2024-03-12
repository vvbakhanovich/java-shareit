package ru.practicum.shareit.request.mapper;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ItemRequestMapperTest {

    private ItemRequestMapper itemRequestMapper;

    @BeforeAll
    public void init() {
        itemRequestMapper = new ItemRequestMapperImpl();
    }

    @Test
    public void mapToModelWithCreatedNotNull() {
        AddItemRequestDto addItemRequestDto = new AddItemRequestDto("description");

        ItemRequest itemRequest = itemRequestMapper.toModel(addItemRequestDto);

        assertThat(itemRequest.getDescription(), is(addItemRequestDto.getDescription()));
        assertThat(itemRequest.getCreated(), is(notNullValue()));
    }

    @Test
    public void mapToItemRequestDtoWithItem() {
        User requester = User.builder().id(1L).build();
        Item item = Item.builder().id(2L).build();
        ItemRequest itemRequest = ItemRequest.builder().requester(requester).build();
        itemRequest.addItem(item);

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertThat(itemRequestDto.getItems().get(0).getRequestId(), is(1));
    }

}