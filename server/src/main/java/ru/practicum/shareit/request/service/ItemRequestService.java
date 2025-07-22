package ru.practicum.shareit.request.service;

import java.util.List;

import ru.practicum.shareit.request.dto.ItemRequestDto;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getRequestsByUser(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);

    ItemRequestDto getRequestById(Long userId, Long requestId);
}
