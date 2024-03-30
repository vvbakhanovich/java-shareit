package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentStorage extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.item i JOIN FETCH c.author u WHERE i.id = ?1")
    List<Comment> findAllByItemId(Long itemId);

    @Query("SELECT c FROM Comment c JOIN FETCH c.item i JOIN FETCH c.author u WHERE i.id IN ?1")
    List<Comment> findAllByItemIdIn(Collection<Long> itemIds);
}
