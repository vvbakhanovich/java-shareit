package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemStorage extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerIdOrderById(Long userId);

    @Query("SELECT i FROM Item i WHERE LOWER(i.name) LIKE ?1 OR LOWER(i.description) LIKE ?1 AND i.available = true")
    List<Item> findAllByNameOrDescriptionContainingIgnoreCase(String text);
}
