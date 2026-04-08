package ru.practicum.shareit.user.dal;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserDbStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<Long, User>();
    private long idCounter = 1L;

    @Override
    public User create(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);

        return user;
    }

    @Override
    public User update(UserUpdateRequestDto user, long id) {
        User userFromMap = users.get(id);

        if (user.getEmail() != null) userFromMap.setEmail(user.getEmail());
        if (user.getName() != null) userFromMap.setName(user.getName());

        return userFromMap;
    }

    @Override
    public List<User> getAll() {
        return users.values()
                .stream()
                .toList();
    }

    @Override
    public void deleteById(long id) {
        users.remove(id);
    }

    @Override
    public User getUserById(long id) {
        return users.get(id);
    }

    @Override
    public boolean existById(long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equals(email));
    }

    @Override
    public boolean existEmailAtOtherId(String email, long id) {
        return users.values().stream()
                .filter(u -> !u.getId().equals(id))
                .anyMatch(user -> user.getEmail() != null && user.getEmail().equals(email));
    }
}
