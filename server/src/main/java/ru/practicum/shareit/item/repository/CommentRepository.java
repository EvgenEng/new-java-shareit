package ru.practicum.shareit.item.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.practicum.shareit.item.model.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByItemIdOrderByCreatedDesc(Long itemId);

    List<Comment> findByItemIdInOrderByCreatedDesc(List<Long> itemIds);

    List<Comment> findByItemId(Long itemId);
}
