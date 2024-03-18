package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.ShortBookingDto;
import ru.practicum.shareit.item.dto.GetItemDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "request.id", target = "requestId")
    ItemDto toDto(Item item);

    Item toModel(ItemDto itemDto);

    List<ItemDto> toDtoList(List<Item> itemList);

    GetItemDto toWithBookingsDto(Item item);

    List<GetItemDto> toWithBookingsDtoList(List<Item> itemList);

    default GetItemDto toGetItemDto(Item item, ShortBookingDto lastBooking, ShortBookingDto nextBooking) {
        return GetItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .build();
    }
}
