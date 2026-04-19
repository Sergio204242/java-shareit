package ru.practicum.shareit.booking.dal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findBookingsByBooker_IdOrderByStartDesc(long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :id AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentByBooker(@Param("id") long id, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :id AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByBooker(@Param("id") long id, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :id AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByBooker(@Param("id") long id, @Param("now") LocalDateTime now);

    List<Booking> findBookingsByBooker_IdAndStatusOrderByStartDesc(long bookerId, Status status);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findBookingsByOwnerIdOrderByStartDesc(@Param("ownerId") long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findPastByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findFutureByOwner(@Param("ownerId") long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findBookingsByOwnerIdAndStatusOrderByStartDesc(@Param("ownerId") long ownerId, @Param("status") Status status);

    @Query("SELECT b FROM Booking b WHERE b.item.itemId IN :itemIds AND b.end < :now AND b.status = 'APPROVED' ORDER BY b.end DESC")
    List<Booking> findLastBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.itemId IN :itemIds AND b.start > :now AND b.status = 'APPROVED' ORDER BY b.start ASC")
    List<Booking> findNextBookingsByItemIds(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.item.itemId = :itemId AND b.booker.id = :userId AND b.status = :status AND b.end < :now")
    boolean existsPastBookingByItemAndUser(@Param("itemId") long itemId, @Param("userId") long userId, @Param("status") Status status, @Param("now") LocalDateTime now);
}
