package ru.practicum.shareit.item.dto.request;

import lombok.Getter;

@Getter
public class ItemUpdateDto {
    private String name;

    private String description;

    private Boolean available;
}
