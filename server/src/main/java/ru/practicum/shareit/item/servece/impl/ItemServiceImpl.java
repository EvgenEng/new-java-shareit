package ru.practicum.shareit.item.servece.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.servece.ItemService;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public List<ItemWithBookingDto> getItemsByOwner(Long userId, int from, int size) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId, pageable);

        return items.stream()
                .map(item -> addBookingInfoToItem(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemWithBookingDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (Objects.equals(item.getOwner().getId(), userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDescAllStatuses(
                    itemId, now, Pageable.ofSize(1));
            Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

            List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAscAllStatuses(
                    itemId, now, Pageable.ofSize(1));
            Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

            return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, commentDtos);
        } else {
            return ItemMapper.toItemWithBookingDto(item, null, null, commentDtos);
        }
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Request not found"));
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User is not owner of this item");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public List<ItemDto> searchItems(String text, int from, int size) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByText(text, pageable);
        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (bookings.isEmpty()) {
            throw new ValidationException("User must have finished booking to leave comment");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(user)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    private ItemWithBookingDto addBookingInfoToItem(Item item, Long userId) {
        List<CommentDto> comments = commentRepository.findByItemId(item.getId()).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDescAllStatuses(
                    item.getId(), now, Pageable.ofSize(1));
            Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);

            List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAscAllStatuses(
                    item.getId(), now, Pageable.ofSize(1));
            Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

            return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, comments);
        }

        return ItemMapper.toItemWithBookingDto(item, null, null, comments);
    }

    @Override
    public List<ItemWithBookingDto> getAllItemsByUserId(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId, pageable);

        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    Long itemId = item.getId();

                    List<Comment> comments = commentRepository.findByItemId(itemId);
                    List<CommentDto> commentDtos = comments.stream()
                            .map(CommentMapper::toCommentDto)
                            .collect(Collectors.toList());

                    List<Booking> lastBookings = bookingRepository.findByItemIdAndEndBeforeOrderByEndDescAllStatuses(
                            itemId, now, Pageable.ofSize(1));
                    Booking lastBooking = lastBookings.isEmpty() ? null : lastBookings.get(0);
                    List<Booking> nextBookings = bookingRepository.findByItemIdAndStartAfterOrderByStartAscAllStatuses(
                            itemId, now, Pageable.ofSize(1));
                    Booking nextBooking = nextBookings.isEmpty() ? null : nextBookings.get(0);

                    return ItemMapper.toItemWithBookingDto(item, lastBooking, nextBooking, commentDtos);
                })
                .collect(Collectors.toList());
    }
}
