package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.request.BookingDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;

import java.util.List;

public interface BookingService {
    BookingResponseDto createBooking(long userId, BookingDto bookingDto);

    BookingResponseDto approveBooking(long bookingId, long userId, boolean approved);

    BookingResponseDto getBooking(long userId, long bookingId);

    List<BookingResponseDto> getBookings(long userId, State state);

    List<BookingResponseDto> getOwnerBookings(long ownerId, State state);
}
