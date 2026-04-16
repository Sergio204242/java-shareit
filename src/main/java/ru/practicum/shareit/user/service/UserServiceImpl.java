package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.duplicate.DuplicateEmailException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.user.dal.UserStorage;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;

    @Override
    public UserResponseDto create(UserCreateRequestDto userDto) {
        if (userStorage.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Email = " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toEntityUserRequestDto(userDto);
        return UserMapper.toResponseDto(userStorage.create(user));
    }

    @Override
    public UserResponseDto update(UserUpdateRequestDto userDto, long id) {
        if (!userStorage.existById(id)) {
            throw new UserNotFoundException("User = " + id + " не найден");
        }
        if (userStorage.existEmailAtOtherId(userDto.getEmail(), id)) {
            throw new DuplicateEmailException("Email = " + userDto.getEmail() +
                    " уже существует у другого пользователя");
        }

        return UserMapper.toResponseDto(userStorage.update(userDto, id));
    }

    @Override
    public List<UserResponseDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    public void deleteById(long id) {
        userStorage.deleteById(id);
    }

    @Override
    public UserResponseDto getUserById(long id) {
        if (!userStorage.existById(id)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        return UserMapper.toResponseDto(userStorage.getUserById(id));
    }
}
