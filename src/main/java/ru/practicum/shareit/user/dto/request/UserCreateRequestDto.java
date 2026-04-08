package ru.practicum.shareit.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UserCreateRequestDto {

    @NotNull
    @NotEmpty(message = "email не может быть пустым")
    @NotBlank(message = "email не должно состоять из пробелов")
    @Email(message = "E-mail некорректный")
    private String email;

    private String name;
}
