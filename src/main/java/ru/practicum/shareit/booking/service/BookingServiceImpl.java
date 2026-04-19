package ru.practicum.shareit.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.request.BookingDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.notfound.BookingNotFoundException;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.StateNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.exception.validation.ValidationException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto createBooking(long userId, BookingDto bookingDto) {

        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("item не найдена"));

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));

        if (item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Нельзя бронировать item");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("item недоступна для бронирования");
        }

        Booking booking = BookingMapper.toEntity(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingResponseDto approveBooking(long bookingId, long userId, boolean approved) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец вещи может подтверждать бронирование");
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ValidationException("Бронирование уже обработано, статус: " + booking.getStatus());
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        bookingRepository.save(booking);

        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingResponseDto getBooking(long userId, long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирование не найдено: " + bookingId));

        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new ValidationException("Только владелец или арендатор может получить бронирование");
        }

        return BookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public List<BookingResponseDto> getBookings(long userId, State state) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        return switch (state) {
            case ALL -> bookingRepository.findBookingsByBooker_IdOrderByStartDesc(userId)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case CURRENT -> bookingRepository.findCurrentByBooker(userId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case PAST -> bookingRepository.findPastByBooker(userId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case FUTURE -> bookingRepository.findFutureByBooker(userId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case WAITING -> bookingRepository.findBookingsByBooker_IdAndStatusOrderByStartDesc(userId, Status.WAITING)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case REJECTED -> bookingRepository.findBookingsByBooker_IdAndStatusOrderByStartDesc(userId, Status.REJECTED)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            default -> throw new StateNotFoundException("Неизвестный статус " + state);
        };
    }

    @Override
    @Transactional
    public List<BookingResponseDto> getOwnerBookings(long ownerId, State state) {

        if (!userRepository.existsById(ownerId)) {
            throw new UserNotFoundException("Пользователь не найден");
        }

        return switch (state) {
            case ALL -> bookingRepository.findBookingsByOwnerIdOrderByStartDesc(ownerId)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case CURRENT -> bookingRepository.findCurrentByOwner(ownerId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case PAST -> bookingRepository.findPastByOwner(ownerId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case FUTURE -> bookingRepository.findFutureByOwner(ownerId, LocalDateTime.now())
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case WAITING -> bookingRepository.findBookingsByOwnerIdAndStatusOrderByStartDesc(ownerId, Status.WAITING)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            case REJECTED -> bookingRepository.findBookingsByOwnerIdAndStatusOrderByStartDesc(ownerId, Status.REJECTED)
                    .stream()
                    .map(BookingMapper::toDto)
                    .toList();
            default -> throw new StateNotFoundException("Неизвестный статус " + state);
        };
    }
}
