package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto create(UserCreateRequestDto user);

    UserResponseDto update(UserUpdateRequestDto userDto, long id);

    List<UserResponseDto> getAll();

    void deleteById(long id);

    UserResponseDto getUserById(long id);
}
