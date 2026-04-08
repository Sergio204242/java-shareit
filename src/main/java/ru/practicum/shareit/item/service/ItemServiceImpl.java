package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.item.dal.ItemStorage;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Override
    public ItemResponseDto createItem(ItemDto itemDto, long userId) {
        if (!userStorage.existById(userId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        Item item = ItemMapper.toEntityItemDto(itemDto, userId);

        return ItemMapper.toResponseDto(itemStorage.create(item));
    }

    @Override
    public ItemResponseDto updateItem(ItemUpdateDto itemDto, long id, long userId) {
        if (!itemStorage.existByUserAndItemIds(id, userId)) {
            throw new ItemNotFoundException("item с id = " + id + " и userId = " + userId + " не найден");
        }

        return ItemMapper.toResponseDto(itemStorage.updateItem(itemDto, id));
    }

    @Override
    public ItemResponseDto getItem(long id) {
        if (!itemStorage.existById(id)) {
            throw new ItemNotFoundException("item с id = " + id + " не найден");
        }

        return ItemMapper.toResponseDto(itemStorage.getItem(id));
    }

    @Override
    public List<ItemResponseDto> getItems(long userId) {
        return itemStorage.getItems(userId).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItem(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        return itemStorage.searchItem(text).stream()
                .map(ItemMapper::toResponseDto)
                .toList();
    }
}