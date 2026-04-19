package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemResponseDto toResponseDto(Item item) {
        ItemResponseDto itemResponseDto = new ItemResponseDto();

        itemResponseDto.setId(item.getItemId());
        itemResponseDto.setName(item.getName());
        itemResponseDto.setDescription(item.getDescription());
        itemResponseDto.setAvailable(item.getAvailable());

        return itemResponseDto;
    }

    public static Item toEntity(ItemDto dto) {
        Item item = new Item();

        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());

        return item;
    }
}
