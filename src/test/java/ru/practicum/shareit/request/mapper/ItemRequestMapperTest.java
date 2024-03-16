package ru.practicum.shareit.request.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ItemMapperImpl.class,
        ItemRequestMapperImpl.class})
class ItemRequestMapperTest {

    @Autowired
    private ItemRequestMapper itemRequestMapper;

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
        ItemRequest itemRequest = ItemRequest.builder().id(1L).requester(requester).build();
        itemRequest.addItem(item);

        ItemRequestDto itemRequestDto = itemRequestMapper.toDto(itemRequest);

        assertThat(itemRequestDto.getItems().get(0).getRequestId(), is(1L));
    }

}