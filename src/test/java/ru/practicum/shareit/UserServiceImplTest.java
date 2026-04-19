package ru.practicum.shareit;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.duplicate.DuplicateEmailException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("create: создание пользователя")
    void createSuccess() {
        UserResponseDto result = userService.create(createDto("alice@example.com", "Alice"));

        assertNotNull(result.getId());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("Alice", result.getName());
    }

    @Test
    @DisplayName("create: дубликат email")
    void createDuplicateEmail() {
        userService.create(createDto("bob@example.com", "Bob"));

        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
                () -> userService.create(createDto("bob@example.com", "Bob2")));

        assertTrue(ex.getMessage().contains("bob@example.com"));
    }

    @Test
    @DisplayName("create: разные email")
    void createDifferentEmails() {
        userService.create(createDto("a@example.com", "A"));
        userService.create(createDto("b@example.com", "B"));

        assertEquals(2, userService.getAll().size());
    }

    @Test
    @DisplayName("update: обновление имени")
    void updateName() {
        UserResponseDto created = userService.create(createDto("c@example.com", "Old"));
        UserResponseDto updated = userService.update(updateDto("c@example.com", "New"), created.getId());

        assertEquals("New", updated.getName());
        assertEquals("c@example.com", updated.getEmail());
    }

    @Test
    @DisplayName("update: обновление email")
    void updateEmail() {
        UserResponseDto created = userService.create(createDto("old@example.com", "User"));
        UserResponseDto updated = userService.update(updateDto("new@example.com", "User"), created.getId());

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("User", updated.getName());
    }

    @Test
    @DisplayName("update: несуществующий id")
    void updateUnknownId() {
        assertThrows(UserNotFoundException.class,
                () -> userService.update(updateDto("x@x.com", "X"), 999_999L));
    }

    @Test
    @DisplayName("update: email занят другим пользователем — DuplicateEmailException")
    void updateEmailTakenByOther() {
        userService.create(createDto("taken@example.com", "First"));
        UserResponseDto second = userService.create(createDto("second@example.com", "Second"));

        assertThrows(DuplicateEmailException.class,
                () -> userService.update(updateDto("taken@example.com", null), second.getId()));
    }

    @Test
    @DisplayName("update: тот же email у того же пользователя")
    void updateSameEmailSameUser() {
        UserResponseDto created = userService.create(createDto("same@example.com", "User"));
        UserResponseDto updated = userService.update(updateDto("same@example.com", "Updated"), created.getId());

        assertEquals("same@example.com", updated.getEmail());
        assertEquals("Updated", updated.getName());
    }

    @Test
    @DisplayName("getAll: пустое хранилище возвращает пустой список")
    void getAllEmpty() {
        assertTrue(userService.getAll().isEmpty());
    }

    @Test
    @DisplayName("getAll: возвращает всех созданных пользователей")
    void getAllMultipleUsers() {
        userService.create(createDto("u1@example.com", "U1"));
        userService.create(createDto("u2@example.com", "U2"));
        userService.create(createDto("u3@example.com", "U3"));

        List<UserResponseDto> all = userService.getAll();
        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("deleteById: пользователь удаляется")
    void deleteById() {
        UserResponseDto created = userService.create(createDto("del@example.com", "Del"));
        userService.deleteById(created.getId());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(created.getId()));
    }

    @Test
    @DisplayName("deleteById: после удаления список уменьшается")
    void deleteByIdListShrinks() {
        UserResponseDto u1 = userService.create(createDto("u1@example.com", "U1"));
        userService.create(createDto("u2@example.com", "U2"));
        userService.deleteById(u1.getId());

        assertEquals(1, userService.getAll().size());
    }

    @Test
    @DisplayName("getUserById: возвращает нужного пользователя")
    void getUserByIdSuccess() {
        UserResponseDto created = userService.create(createDto("find@example.com", "Find"));
        UserResponseDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("find@example.com", found.getEmail());
    }

    @Test
    @DisplayName("getUserById: несуществующий id")
    void getUserByIdUnknownId() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999_999L));
    }

    private UserCreateRequestDto createDto(String email, String name) {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        setField(dto, "email", email);
        setField(dto, "name", name);
        return dto;
    }

    private UserUpdateRequestDto updateDto(String email, String name) {
        UserUpdateRequestDto dto = new UserUpdateRequestDto();
        if (email != null) setField(dto, "email", email);
        if (name != null) setField(dto, "name", name);
        return dto;
    }

    private void setField(Object obj, String field, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
