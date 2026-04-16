package ru.practicum.shareit.item.dto.response;

import lombok.Data;

@Data
public class ItemResponseDto {
    private Long id;
    private String name;
    private String description;
    private boolean available;
}
