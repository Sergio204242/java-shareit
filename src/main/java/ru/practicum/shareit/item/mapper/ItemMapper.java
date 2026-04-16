package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemResponseDto toResponseDto(Item item) {
        ItemResponseDto itemResponseDto = new ItemResponseDto();

        itemResponseDto.setName(item.getName());
        itemResponseDto.setDescription(item.getDescription());
        itemResponseDto.setAvailable(item.getAvailable());
        itemResponseDto.setId(item.getItemId());

        return itemResponseDto;
    }

    public static Item toEntityItemDto(ItemDto itemDto, long userId) {
        Item item = new Item();

        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setUserId(userId);

        return item;
    }
}
