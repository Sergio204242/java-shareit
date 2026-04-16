package ru.practicum.shareit.item.dal;

import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage {
    Item create(Item item);

    Item updateItem(ItemUpdateDto item, long id);

    Item getItem(long id);

    List<Item> getItems(long userId);

    List<Item> searchItem(String text);

    boolean existByUserAndItemIds(long id, long userId);

    boolean existById(long id);
}
