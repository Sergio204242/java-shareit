package ru.practicum.shareit.item.model;

import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {
    private long itemId;
    private String name;
    private String description;
    private Boolean available;
    private long userId;
}
