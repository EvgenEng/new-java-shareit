package ru.practicum.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.exception.InvalidCommentException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.item.dto.CommentDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    @Override
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Adding comment to item {} by user {}", itemId, userId);
        User author = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", userId);
                    return new NotFoundException("User not found");
                });

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> {
                    log.error("Item with id {} not found", itemId);
                    return new NotFoundException("Item not found");
                });

        validateUserBookedItem(itemId, userId);

        Comment comment = commentMapper.toEntity(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        log.debug("Added comment with id {}", savedComment.getId());

        return commentMapper.toDto(savedComment);
    }

    private void validateUserBookedItem(Long itemId, Long userId) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndEndBeforeAndStatus(
                itemId,
                userId,
                now,
                Booking.BookingStatus.APPROVED // Добавляем проверку статуса
        );

        if (bookings.isEmpty()) {
            log.error("User {} never booked item {} or booking not completed", userId, itemId);
            throw new InvalidCommentException("User never booked this item or booking not completed");
        }
    }
}
