package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;
import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.id = ?1")
    List<Booking> findAllByItemId(Long itemId);

    @Query("select b from Booking b JOIN FETCH b.item i JOIN FETCH b.booker u WHERE i.id = ?1 AND u.id = ?2")
    List<Booking> findAllByItemIdAndBookerId(Long itemId, Long bookerId);

    List<Booking> findAllByItemIdIn(Collection<Long> itemIds);
}
