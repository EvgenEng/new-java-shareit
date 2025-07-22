package ru.practicum.item;

import ru.practicum.item.dto.CommentDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.InvalidCommentException;

public interface CommentService {
    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto)
            throws NotFoundException, InvalidCommentException;
}
