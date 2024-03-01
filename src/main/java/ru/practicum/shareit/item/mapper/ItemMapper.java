package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemDto toDto(Item item);

    Item toModel(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> itemList);

    ItemWithBookingsDto toWithBookingsDto(Item item);

    List<ItemWithBookingsDto> toWithBookingsDtoList(List<Item> itemList);
}
