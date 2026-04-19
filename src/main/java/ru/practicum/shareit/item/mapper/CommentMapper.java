package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.model.Comment;

public class CommentMapper {

    public static CommentResponseDto toDto(Comment comment) {

        CommentResponseDto dto = new CommentResponseDto();

        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());

        return dto;
    }
}
