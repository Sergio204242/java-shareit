package ru.practicum.shareit.item.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentDto {

    @NotBlank(message = "Текст комментария не может быть пустым")
    private String text;
}
