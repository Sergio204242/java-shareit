package ru.practicum.shareit.user.dal;

import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserStorage {

    User create(User user);

    User update(UserUpdateRequestDto userDto, long id);

    List<User> getAll();

    void deleteById(long id);

    User getUserById(long id);

    boolean existById(long id);

    boolean existsByEmail(String email);

    boolean existEmailAtOtherId(String email, long id);
}
