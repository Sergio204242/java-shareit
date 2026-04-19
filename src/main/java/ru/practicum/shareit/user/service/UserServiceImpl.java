package ru.practicum.shareit.user.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.duplicate.DuplicateEmailException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDto create(UserCreateRequestDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Email = " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toEntityUserRequestDto(userDto);
        return UserMapper.toResponseDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponseDto update(UserUpdateRequestDto userDto, long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User = " + id + " не найден");
        }
        if (userRepository.existsByEmailAndIdNot(userDto.getEmail(), id)) {
            throw new DuplicateEmailException("Email = " + userDto.getEmail() +
                    " уже существует у другого пользователя");
        }

        if (userDto.getName() != null) {
            userRepository.updateName(userDto.getName(), id);
        }
        if (userDto.getEmail() != null) {
            userRepository.updateEmail(userDto.getEmail(), id);
        }

        return UserMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User = " + id + " не найден")));
    }

    @Override
    @Transactional
    public List<UserResponseDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponseDto getUserById(long id) {
        return UserMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User = " + id + " не найден")));
    }
}
