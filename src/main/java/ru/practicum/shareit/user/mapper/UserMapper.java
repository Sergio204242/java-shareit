package ru.practicum.shareit.user.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.model.User;

@Component
public class UserMapper {
    public static UserResponseDto toResponseDto(User user) {

        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getName());
    }

    public static User toEntityUserRequestDto(UserCreateRequestDto dto) {
        if (dto == null) return null;

        User user = new User();

        user.setEmail(dto.getEmail());
        user.setName(dto.getName());

        return user;
    }
}
