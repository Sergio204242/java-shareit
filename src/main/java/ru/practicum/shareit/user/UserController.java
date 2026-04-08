package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.dto.request.UserUpdateRequestDto;
import ru.practicum.shareit.user.dto.response.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    public UserResponseDto createUser(@Valid @NotNull @RequestBody UserCreateRequestDto user) {
        return userService.create(user);
    }

    @PatchMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable @NotNull @Positive long id,
                                      @Valid @NotNull @RequestBody UserUpdateRequestDto user) {
        return userService.update(user, id);
    }

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable @Positive long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable @Positive Long id) {
        userService.deleteById(id);
    }
}
