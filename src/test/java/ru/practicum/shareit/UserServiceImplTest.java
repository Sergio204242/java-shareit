package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.duplicate.DuplicateEmailException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.user.dal.UserDbStorage;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceImplTest {

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(new UserDbStorage());
    }

    @Test
    @DisplayName("create: создание пользователя")
    void createSuccess() {
        UserCreateRequestDto dto = buildCreateDto("alice@example.com", "Alice");

        UserResponseDto result = userService.create(dto);

        assertNotNull(result.getId());
        assertEquals("alice@example.com", result.getEmail());
        assertEquals("Alice", result.getName());
    }

    @Test
    @DisplayName("create: дубликат email")
    void createDuplicateEmail() {
        userService.create(buildCreateDto("bob@example.com", "Bob"));

        DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
                () -> userService.create(buildCreateDto("bob@example.com", "Bob2")));

        assertTrue(ex.getMessage().contains("bob@example.com"));
    }

    @Test
    @DisplayName("create: разные email")
    void createDifferentEmails() {
        userService.create(buildCreateDto("a@example.com", "A"));
        userService.create(buildCreateDto("b@example.com", "B"));

        assertEquals(2, userService.getAll().size());
    }

    @Test
    @DisplayName("update: обновление имени")
    void updateName() {
        UserResponseDto created = userService.create(buildCreateDto("c@example.com", "Old Name"));

        UserUpdateRequestDto updateDto = buildUpdateDto(null, "New Name");
        UserResponseDto updated = userService.update(updateDto, created.getId());

        assertEquals("New Name", updated.getName());
        assertEquals("c@example.com", updated.getEmail());
    }

    @Test
    @DisplayName("update: обновление email")
    void updateEmail() {
        UserResponseDto created = userService.create(buildCreateDto("old@example.com", "User"));

        UserUpdateRequestDto updateDto = buildUpdateDto("new@example.com", null);
        UserResponseDto updated = userService.update(updateDto, created.getId());

        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    @DisplayName("update: несуществующий id")
    void updateUnknownId() {
        assertThrows(UserNotFoundException.class,
                () -> userService.update(buildUpdateDto("x@x.com", "X"), 999L));
    }

    @Test
    @DisplayName("update: email занят другим пользователем")
    void updateEmailTakenByOther() {
        userService.create(buildCreateDto("taken@example.com", "First"));
        UserResponseDto second = userService.create(buildCreateDto("second@example.com", "Second"));

        assertThrows(DuplicateEmailException.class,
                () -> userService.update(buildUpdateDto("taken@example.com", null), second.getId()));
    }

    @Test
    @DisplayName("update: тот же email у того же пользователя")
    void updateSameEmailSameUser() {
        UserResponseDto created = userService.create(buildCreateDto("same@example.com", "User"));

        UserUpdateRequestDto updateDto = buildUpdateDto("same@example.com", "Updated Name");
        UserResponseDto updated = userService.update(updateDto, created.getId());

        assertEquals("same@example.com", updated.getEmail());
        assertEquals("Updated Name", updated.getName());
    }

    @Test
    @DisplayName("getAll: пустое хранилище возвращает пустой список")
    void getAllEmpty() {
        assertTrue(userService.getAll().isEmpty());
    }

    @Test
    @DisplayName("getAll: возвращает всех созданных пользователей")
    void getAllMultipleUsers() {
        userService.create(buildCreateDto("u1@example.com", "U1"));
        userService.create(buildCreateDto("u2@example.com", "U2"));
        userService.create(buildCreateDto("u3@example.com", "U3"));

        List<UserResponseDto> all = userService.getAll();

        assertEquals(3, all.size());
    }

    @Test
    @DisplayName("deleteById: пользователь удаляется")
    void deleteById() {
        UserResponseDto created = userService.create(buildCreateDto("del@example.com", "Del"));

        userService.deleteById(created.getId());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(created.getId()));
    }

    @Test
    @DisplayName("deleteById: после удаления список уменьшается")
    void deleteByIdListShrinks() {
        UserResponseDto u1 = userService.create(buildCreateDto("u1@example.com", "U1"));
        userService.create(buildCreateDto("u2@example.com", "U2"));

        userService.deleteById(u1.getId());

        assertEquals(1, userService.getAll().size());
    }

    @Test
    @DisplayName("getUserById: возвращает нужного пользователя")
    void getUserByIdSuccess() {
        UserResponseDto created = userService.create(buildCreateDto("find@example.com", "Find"));

        UserResponseDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("find@example.com", found.getEmail());
    }

    @Test
    @DisplayName("getUserById: несуществующий id")
    void getUserByIdUnknownId() {
        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(42L));
    }

    private UserCreateRequestDto buildCreateDto(String email, String name) {
        try {
            UserCreateRequestDto dto = new UserCreateRequestDto();
            setField(dto, "email", email);
            setField(dto, "name", name);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private UserUpdateRequestDto buildUpdateDto(String email, String name) {
        try {
            UserUpdateRequestDto dto = new UserUpdateRequestDto();
            if (email != null) setField(dto, "email", email);
            if (name != null) setField(dto, "name", name);
            return dto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
