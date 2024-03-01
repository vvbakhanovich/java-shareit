package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item> {
    List<Item> findAllByOwnerIdOrderById(Long userId);
}
