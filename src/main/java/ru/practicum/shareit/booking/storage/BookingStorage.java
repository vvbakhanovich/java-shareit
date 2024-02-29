package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingStorage extends JpaRepository<Booking, Long>, QuerydslPredicateExecutor<Booking> {

    List<Booking> findAllByBookerIdOrderByStartAsc(Long bookerId);
}
