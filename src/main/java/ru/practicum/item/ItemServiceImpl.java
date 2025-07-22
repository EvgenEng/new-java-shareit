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
import ru.practicum.item.dto.ItemDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long ownerId) {
        log.info("Creating item for owner {}", ownerId);
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("User with id {} not found", ownerId);
                    return new NotFoundException("User not found");
                });

        Item item = itemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        log.debug("Created item with id {}", savedItem.getId());

        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long ownerId) {
        log.info("Updating item {} for owner {}", itemDto.getId(), ownerId);
        Item existingItem = itemRepository.findById(itemDto.getId())
                .orElseThrow(() -> {
                    log.error("Item with id {} not found", itemDto.getId());
                    return new NotFoundException("Item not found");
                });

        if (!existingItem.getOwner().getId().equals(ownerId)) {
            log.error("User {} is not owner of item {}", ownerId, itemDto.getId());
            throw new NotFoundException("Only owner can update item");
        }

        itemMapper.updateItemFromDto(itemDto, existingItem);
        Item updatedItem = itemRepository.save(existingItem);
        log.debug("Updated item with id {}", updatedItem.getId());

        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long id, Long ownerId) {
        log.info("Getting item {} for user {}", id, ownerId);
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item with id {} not found", id);
                    return new NotFoundException("Item not found");
                });

        return enrichAndConvertToDto(item, ownerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllByOwner(Long ownerId) {
        log.info("Getting all items for owner {}", ownerId);
        return itemRepository.findByOwnerIdOrderById(ownerId).stream()
                .map(item -> enrichAndConvertToDto(item, ownerId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        log.info("Searching items by text: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
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

    private ItemDto enrichAndConvertToDto(Item item, Long ownerId) {
        ItemDto itemDto = itemMapper.toItemDto(item);
        enrichItemDtoWithAdditionalData(itemDto, item, ownerId);
        return itemDto;
    }

    private void enrichItemDtoWithAdditionalData(ItemDto itemDto, Item item, Long ownerId) {
        if (item.getOwner().getId().equals(ownerId)) {
            LocalDateTime now = LocalDateTime.now();
            addBookingInfo(itemDto, item.getId(), now);
        }
        addCommentsInfo(itemDto, item.getId());
    }

    private void addBookingInfo(ItemDto itemDto, Long itemId, LocalDateTime now) {
        bookingRepository.findLastBooking(itemId, now).stream()
                .findFirst()
                .ifPresent(booking -> itemDto.setLastBooking(
                        new ItemDto.BookingShort(booking.getId(), booking.getBooker().getId())));

        bookingRepository.findNextBooking(itemId, now).stream()
                .findFirst()
                .ifPresent(booking -> itemDto.setNextBooking(
                        new ItemDto.BookingShort(booking.getId(), booking.getBooker().getId())));
    }

    private void addCommentsInfo(ItemDto itemDto, Long itemId) {
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);
    }

    private void validateUserBookedItem(Long itemId, Long userId) {
        List<Booking> bookings = bookingRepository.findCompletedBookings(
                itemId, userId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            log.error("User {} never booked item {}", userId, itemId);
            throw new InvalidCommentException("User never booked this item");
        }
    }
}
