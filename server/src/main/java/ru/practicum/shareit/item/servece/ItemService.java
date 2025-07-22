package ru.practicum.shareit.item.servece;

import java.util.List;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;

public interface ItemService {

    List<ItemWithBookingDto> getItemsByOwner(Long userId, int from, int size);

    List<ItemWithBookingDto> getAllItemsByUserId(Long userId, Integer from, Integer size);

    ItemWithBookingDto getItemById(Long itemId, Long userId);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto addComment(Long itemId, Long userId, CommentDto commentDto);
}
