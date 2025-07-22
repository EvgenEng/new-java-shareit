package ru.practicum.item;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.practicum.item.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemId(Long itemId);

    List<Comment> findByItemOwnerId(Long ownerId);
}
