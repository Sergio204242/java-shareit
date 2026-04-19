package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.request.CommentDto;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto createItem(ItemDto itemDto, long us);

    ItemResponseDto updateItem(ItemUpdateDto itemDto, long id, long us);

    ItemResponseDto getItem(long id);

    List<ItemResponseDto> getItems(long userId);

    List<ItemResponseDto> searchItem(String text);

    CommentResponseDto addComment(long userId, long itemId, CommentDto commentDto);
}
