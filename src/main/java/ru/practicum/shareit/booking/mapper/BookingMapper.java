package ru.practicum.shareit.booking.mapper;


import ru.practicum.shareit.booking.dto.request.BookingDto;
import ru.practicum.shareit.booking.dto.response.BookerDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.dto.response.ItemDtoForBookingDto;

public class BookingMapper {

    public static BookingResponseDto toDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        BookerDto bookerDto = new BookerDto();
        bookerDto.setId(booking.getBooker().getId());
        dto.setBooker(bookerDto);

        ItemDtoForBookingDto itemDto = new ItemDtoForBookingDto();
        itemDto.setId(booking.getItem().getItemId());
        itemDto.setName(booking.getItem().getName());
        dto.setItem(itemDto);

        return dto;
    }

    public static Booking toEntity(BookingDto bookingDto) {

        Booking booking = new Booking();

        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());

        return booking;
    }
}