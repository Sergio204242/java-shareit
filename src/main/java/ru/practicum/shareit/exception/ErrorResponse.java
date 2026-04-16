package ru.practicum.shareit.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorResponse {

    private String error;

    private String debugMessage;

    public ErrorResponse(String message, String debugMessage) {
        this.error = message;
        this.debugMessage = debugMessage;
    }
}
