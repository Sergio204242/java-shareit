package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dal.BookingRepository;
import ru.practicum.shareit.booking.dto.request.BookingDto;
import ru.practicum.shareit.booking.dto.response.BookingResponseDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.notfound.BookingNotFoundException;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.exception.validation.ValidationException;
import ru.practicum.shareit.item.dal.ItemRepository;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dal.UserRepository;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private UserService userService;
    @Autowired
    private ItemService itemService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    private long ownerId;
    private long bookerId;
    private long itemId;

    private static LocalDateTime plusDays(int days) {
        return LocalDateTime.now().plusDays(days);
    }

    @BeforeEach
    void setUp() {
        ownerId = userService.create(userDto("owner@example.com", "Owner")).getId();
        bookerId = userService.create(userDto("booker@example.com", "Booker")).getId();
        itemId = itemService.createItem(itemDto("Дрель", "Мощная", true), ownerId).getId();
    }

    @Test
    @DisplayName("createBooking: успешное создание")
    void createBookingSuccess() {
        BookingResponseDto result = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        assertNotNull(result.getId());
        assertEquals(Status.WAITING, result.getStatus());
        assertEquals(itemId, result.getItem().getId());
    }

    @Test
    @DisplayName("createBooking: end раньше start")
    void createBookingEndBeforeStart() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(bookerId,
                        bookingDto(itemId, plusDays(2), plusDays(1))));
    }

    @Test
    @DisplayName("createBooking: несуществующая вещь")
    void createBookingUnknownItem() {
        assertThrows(ItemNotFoundException.class, () ->
                bookingService.createBooking(bookerId,
                        bookingDto(999_999L, plusDays(1), plusDays(2))));
    }

    @Test
    @DisplayName("createBooking: несуществующий букер")
    void createBookingUnknownUser() {
        assertThrows(UserNotFoundException.class, () ->
                bookingService.createBooking(999_999L,
                        bookingDto(itemId, plusDays(1), plusDays(2))));
    }

    @Test
    @DisplayName("createBooking: владелец не может бронировать свою вещь")
    void createBookingByOwner() {
        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(ownerId,
                        bookingDto(itemId, plusDays(1), plusDays(2))));
    }

    @Test
    @DisplayName("createBooking: недоступная вещь")
    void createBookingUnavailableItem() {
        long unavailableItemId = itemService.createItem(itemDto("Занятая", "Описание", false), ownerId).getId();

        assertThrows(ValidationException.class, () ->
                bookingService.createBooking(bookerId,
                        bookingDto(unavailableItemId, plusDays(1), plusDays(2))));
    }

    @Test
    @DisplayName("approveBooking: APPROVED корректно устанавливается")
    void approveBookingApproved() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        BookingResponseDto approved = bookingService.approveBooking(created.getId(), ownerId, true);

        assertEquals(Status.APPROVED, approved.getStatus());
    }

    @Test
    @DisplayName("approveBooking: REJECTED корректно устанавливается")
    void approveBookingRejected() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        BookingResponseDto rejected = bookingService.approveBooking(created.getId(), ownerId, false);

        assertEquals(Status.REJECTED, rejected.getStatus());
    }

    @Test
    @DisplayName("approveBooking: не владелец")
    void approveBookingNotOwner() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(created.getId(), bookerId, true));
    }

    @Test
    @DisplayName("approveBooking: уже обработанное бронирование")
    void approveBookingAlreadyProcessed() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));
        bookingService.approveBooking(created.getId(), ownerId, true);

        assertThrows(ValidationException.class, () ->
                bookingService.approveBooking(created.getId(), ownerId, false));
    }

    @Test
    @DisplayName("approveBooking: несуществующее бронирование")
    void approveBookingNotFound() {
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.approveBooking(999_999L, ownerId, true));
    }

    @Test
    @DisplayName("getBooking: владелец видит бронирование")
    void getBookingByOwner() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        BookingResponseDto found = bookingService.getBooking(ownerId, created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("getBooking: арендатор видит бронирование")
    void getBookingByBooker() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        BookingResponseDto found = bookingService.getBooking(bookerId, created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    @DisplayName("getBooking: посторонний")
    void getBookingByStranger() {
        long stranger = userService.create(userDto("stranger@example.com", "Stranger")).getId();
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));

        assertThrows(ValidationException.class, () ->
                bookingService.getBooking(stranger, created.getId()));
    }

    @Test
    @DisplayName("getBooking: несуществующее бронирование")
    void getBookingNotFound() {
        assertThrows(BookingNotFoundException.class, () ->
                bookingService.getBooking(ownerId, 999_999L));
    }

    @Test
    @DisplayName("getBookings ALL: возвращает все бронирования букера")
    void getBookingsAll() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(1), plusDays(2)));
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(3), plusDays(4)));

        List<BookingResponseDto> list = bookingService.getBookings(bookerId, State.ALL);
        assertEquals(2, list.size());
    }

    @Test
    @DisplayName("getBookings FUTURE: только будущие бронирования")
    void getBookingsFuture() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(5), plusDays(6)));
        savePastApprovedBooking(itemId, bookerId);

        List<BookingResponseDto> future = bookingService.getBookings(bookerId, State.FUTURE);
        assertTrue(future.stream().allMatch(b -> b.getStart().isAfter(LocalDateTime.now())));
    }

    @Test
    @DisplayName("getBookings PAST: только прошедшие бронирования")
    void getBookingsPast() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(1), plusDays(2)));
        savePastApprovedBooking(itemId, bookerId);

        List<BookingResponseDto> past = bookingService.getBookings(bookerId, State.PAST);
        assertEquals(1, past.size());
        assertTrue(past.get(0).getEnd().isBefore(LocalDateTime.now()));
    }

    @Test
    @DisplayName("getBookings WAITING: только бронирования со статусом WAITING")
    void getBookingsWaiting() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(1), plusDays(2)));

        List<BookingResponseDto> waiting = bookingService.getBookings(bookerId, State.WAITING);
        assertTrue(waiting.stream().allMatch(b -> b.getStatus() == Status.WAITING));
    }

    @Test
    @DisplayName("getBookings REJECTED: только отклонённые бронирования")
    void getBookingsRejected() {
        BookingResponseDto created = bookingService.createBooking(bookerId,
                bookingDto(itemId, plusDays(1), plusDays(2)));
        bookingService.approveBooking(created.getId(), ownerId, false);

        List<BookingResponseDto> rejected = bookingService.getBookings(bookerId, State.REJECTED);
        assertEquals(1, rejected.size());
        assertEquals(Status.REJECTED, rejected.get(0).getStatus());
    }

    @Test
    @DisplayName("getBookings: несуществующий пользователь")
    void getBookingsUnknownUser() {
        assertThrows(UserNotFoundException.class, () ->
                bookingService.getBookings(999_999L, State.ALL));
    }

    @Test
    @DisplayName("getOwnerBookings ALL: владелец видит бронирования своих вещей")
    void getOwnerBookingsAll() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(1), plusDays(2)));

        List<BookingResponseDto> list = bookingService.getOwnerBookings(ownerId, State.ALL);
        assertEquals(1, list.size());
    }

    @Test
    @DisplayName("getOwnerBookings WAITING: только ожидающие подтверждения")
    void getOwnerBookingsWaiting() {
        bookingService.createBooking(bookerId, bookingDto(itemId, plusDays(1), plusDays(2)));

        List<BookingResponseDto> list = bookingService.getOwnerBookings(ownerId, State.WAITING);
        assertTrue(list.stream().allMatch(b -> b.getStatus() == Status.WAITING));
    }

    @Test
    @DisplayName("getOwnerBookings PAST: только завершённые бронирования вещей владельца")
    void getOwnerBookingsPast() {
        savePastApprovedBooking(itemId, bookerId);

        List<BookingResponseDto> past = bookingService.getOwnerBookings(ownerId, State.PAST);
        assertEquals(1, past.size());
    }

    @Test
    @DisplayName("getOwnerBookings: несуществующий владелец")
    void getOwnerBookingsUnknownUser() {
        assertThrows(UserNotFoundException.class, () ->
                bookingService.getOwnerBookings(999_999L, State.ALL));
    }

    private void savePastApprovedBooking(long itemId, long bookerId) {
        Booking booking = new Booking();
        booking.setItem(itemRepository.findById(itemId).orElseThrow());
        booking.setBooker(userRepository.findById(bookerId).orElseThrow());
        booking.setStart(LocalDateTime.now().minusDays(3));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);
    }

    private BookingDto bookingDto(long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

    private UserCreateRequestDto userDto(String email, String name) {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        setField(dto, "email", email);
        setField(dto, "name", name);
        return dto;
    }

    private ItemDto itemDto(String name, String description, boolean available) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
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