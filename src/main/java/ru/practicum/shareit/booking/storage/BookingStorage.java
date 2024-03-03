package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BookingStorage extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.id = ?1")
    Optional<Booking> findBookingById(Long bookingId);

    @Query("SELECT b FROM Booking b JOIN b.item i JOIN FETCH b.booker u WHERE i.id = ?1")
    List<Booking> findAllByItemId(Long itemId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.id = ?1 AND u.id = ?2")
    List<Booking> findAllByItemIdAndBookerId(Long itemId, Long bookerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.id IN ?1")
    List<Booking> findAllByItemIdIn(Collection<Long> itemIds);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.item.owner.id = ?1 ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.owner.id = ?1 AND b.start <= ?2 AND b.end >= ?3 ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime startBefore,
                                                                                LocalDateTime endAfter);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.owner.id = ?1 AND b.end <= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime endBefore);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.owner.id = ?1 AND b.start >= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime startAfter);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.owner.id = ?1 AND b.status = ?2")
    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.booker.id = ?1 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdOrderByStartDesc(Long ownerId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.booker.id = ?1 AND b.start <= ?2 AND b.end >= ?3 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId, LocalDateTime startBefore,
                                                                                LocalDateTime endAfter);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.booker.id = ?1 AND b.end <= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long ownerId, LocalDateTime endBefore);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.booker.id = ?1 AND b.start >= ?2 ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long ownerId, LocalDateTime startAfter);

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE b.booker.id = ?1 AND b.status = ?2")
    List<Booking> findByBookerIdAndStatus(Long ownerId, BookingStatus status);
}
