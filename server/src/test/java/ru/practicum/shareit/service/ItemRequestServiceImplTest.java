package ru.practicum.shareit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.impl.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository itemRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private User otherUser;
    private ItemRequest itemRequest;
    private ItemRequestDto itemRequestDto;
    private Item item;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("User").email("user@example.com").build();
        otherUser = User.builder().id(2L).name("Other User").email("other@example.com").build();

        itemRequest = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requester(user)
                .created(LocalDateTime.now())
                .build();

        itemRequestDto = ItemRequestDto.builder()
                .description("Need a drill")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(otherUser)
                .request(itemRequest)
                .build();
    }

    @Test
    void shouldCreateItemRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.save(any(ItemRequest.class))).thenReturn(itemRequest);

        ItemRequestDto result = itemRequestService.createRequest(1L, itemRequestDto);

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        assertNotNull(result.getCreated());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingRequestForNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.createRequest(99L, itemRequestDto));
    }

    @Test
    void shouldGetItemRequestsByUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findByRequesterIdOrderByCreatedDesc(1L)).thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getRequestsByUser(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void shouldGetAllItemRequestsPaginated() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRequestRepository.findByRequesterIdNotOrderByCreatedDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(itemRequest));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        List<ItemRequestDto> result = itemRequestService.getAllRequests(2L, 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Need a drill", result.get(0).getDescription());
        assertEquals(1, result.get(0).getItems().size());
    }

    @Test
    void shouldGetItemRequestById() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));
        when(itemRequestRepository.findById(1L)).thenReturn(Optional.of(itemRequest));
        when(itemRepository.findByRequestId(1L)).thenReturn(List.of(item));

        ItemRequestDto result = itemRequestService.getRequestById(2L, 1L);

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        assertEquals(1, result.getItems().size());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemRequestService.getRequestById(1L, 99L));
    }
}
