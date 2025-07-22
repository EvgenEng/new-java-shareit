package ru.practicum.shareit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.impl.BookingServiceImpl;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User owner;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("John Doe").email("john@example.com").build();
        owner = User.builder().id(2L).name("Jane Doe").email("jane@example.com").build();
        item = Item.builder().id(1L).name("Drill").description("Powerful drill").available(true).owner(owner).build();

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        booking = Booking.builder().id(1L).start(start).end(end).item(item).booker(user).status(BookingStatus.WAITING)
                .build();
        bookingDto = BookingDto.builder().itemId(1L).start(start).end(end).build();
    }

    @Test
    void shouldCreateBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponseDto result = bookingService.createBooking(1L, bookingDto);

        assertNotNull(result);
        assertEquals(BookingStatus.WAITING, result.getStatus());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingBookingForNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(99L, bookingDto));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenCreatingBookingForNonExistentItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        bookingDto.setItemId(99L);

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(1L, bookingDto));
        assertEquals("Item not found", exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWhenItemNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, bookingDto));
        assertEquals("Item is not available for booking", exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWhenOwnerTriesToBook() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(2L, bookingDto));
        assertEquals("Owner cannot book their own item", exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWithInvalidDates() {
        BookingDto invalidDto = BookingDto.builder()
                .itemId(1L)
                .start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(1)) // end before start
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, invalidDto));
        assertEquals("End date must be after start date", exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWithEqualStartAndEndDates() {
        LocalDateTime sameTime = LocalDateTime.now().plusDays(1);
        BookingDto invalidDto = BookingDto.builder()
                .itemId(1L)
                .start(sameTime)
                .end(sameTime) // end equals start
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.createBooking(1L, invalidDto));
        assertEquals("End date must be after start date", exception.getMessage());
    }

    @Test
    void shouldUpdateBookingStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponseDto result = bookingService.updateBookingStatus(2L, 1L, true);

        assertNotNull(result);
        assertEquals(BookingStatus.APPROVED, result.getStatus());
    }

    @Test
    void shouldRejectBookingStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponseDto result = bookingService.updateBookingStatus(2L, 1L, false);

        assertNotNull(result);
        assertEquals(BookingStatus.REJECTED, result.getStatus());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenUpdatingNonExistentBooking() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.updateBookingStatus(2L, 99L, true));
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void shouldNotUpdateBookingStatusWhenNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> bookingService.updateBookingStatus(1L, 1L, true)); // user is not owner
        assertEquals("Only item owner can update booking status", exception.getMessage());
    }

    @Test
    void shouldNotUpdateBookingStatusWhenNotWaiting() {
        Booking approvedBooking = Booking.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user)
                .status(BookingStatus.APPROVED) // уже подтверждено
                .build();

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(approvedBooking));

        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.updateBookingStatus(2L, 1L, true));
        assertEquals("Booking status can only be changed from WAITING", exception.getMessage());
    }

    @Test
    void shouldGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldGetBookingByIdForOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponseDto result = bookingService.getBookingById(2L, 1L); // owner

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingNonExistentBooking() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(1L, 99L));
        assertEquals("Booking not found", exception.getMessage());
    }

    @Test
    void shouldNotGetBookingByIdForOtherUser() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingById(99L, 1L)); // other user
        assertEquals("User can only view their own bookings or bookings for their items", exception.getMessage());
    }

    @Test
    void shouldGetUserBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "ALL", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingBookingsForNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByUser(99L, "ALL", 0, 10));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldGetUserBookingsWithCurrentState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndCurrentOrderByStartDesc(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "CURRENT", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithPastState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndPastOrderByStartDesc(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "PAST", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithFutureState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndFutureOrderByStartDesc(eq(1L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "FUTURE", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithWaitingState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(1L), eq(BookingStatus.WAITING),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "WAITING", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithRejectedState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdAndStatusOrderByStartDesc(eq(1L), eq(BookingStatus.REJECTED),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "REJECTED", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetUserBookingsWithUnknownState() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "UNKNOWN", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "ALL", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldThrowNotFoundExceptionWhenGettingOwnerBookingsForNonExistentUser() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.getBookingsByOwner(99L, "ALL", 0, 10));
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldGetOwnerBookingsWithCurrentState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdAndCurrentOrderByStartDesc(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "CURRENT", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookingsWithPastState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdAndPastOrderByStartDesc(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "PAST", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookingsWithFutureState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdAndFutureOrderByStartDesc(eq(2L), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "FUTURE", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookingsWithWaitingState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDesc(eq(2L), eq(BookingStatus.WAITING),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "WAITING", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookingsWithRejectedState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdAndStatusOrderByStartDesc(eq(2L), eq(BookingStatus.REJECTED),
                any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "REJECTED", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldGetOwnerBookingsWithUnknownState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "UNKNOWN", 0, 10);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoUserBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(1L), any(Pageable.class)))
                .thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getBookingsByUser(1L, "ALL", 0, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyListWhenNoOwnerBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItem_OwnerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of());

        List<BookingResponseDto> result = bookingService.getBookingsByOwner(2L, "ALL", 0, 10);

        assertTrue(result.isEmpty());
    }
}