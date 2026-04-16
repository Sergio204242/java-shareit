package ru.practicum.shareit.item.dal;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ItemDbStorage implements ItemStorage {
    private final Map<Long, Item> items = new HashMap<Long, Item>();
    private long idCounter = 1L;

    @Override
    public Item create(Item item) {
        item.setItemId(idCounter++);
        items.put(item.getItemId(), item);

        return item;
    }

    @Override
    public Item updateItem(ItemUpdateDto item, long id) {
        Item itemFromMap = items.get(id);

        if (item.getName() != null) itemFromMap.setName(item.getName());
        if (item.getDescription() != null) itemFromMap.setDescription(item.getDescription());
        if (item.getAvailable() != null) itemFromMap.setAvailable(item.getAvailable());

        return itemFromMap;
    }

    @Override
    public Item getItem(long id) {
        return items.get(id);
    }

    @Override
    public List<Item> getItems(long userId) {
        return items.values().stream()
                .filter(item -> item.getUserId() == userId)
                .toList();
    }

    @Override
    public List<Item> searchItem(String text) {
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                                item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .toList();
    }

    @Override
    public boolean existByUserAndItemIds(long id, long userId) {
        return items.containsKey(id) && items.get(id).getUserId() == userId;
    }

    @Override
    public boolean existById(long id) {
        return items.containsKey(id);
    }
}