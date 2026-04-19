package ru.practicum.shareit.booking.dto.response;

import lombok.Data;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.user.dto.response.ItemDtoForBookingDto;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {

    private long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Status status;

    private BookerDto booker;

    private ItemDtoForBookingDto item;

}
