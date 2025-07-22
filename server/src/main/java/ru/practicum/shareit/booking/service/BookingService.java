package ru.practicum.shareit.booking.service;

import java.util.List;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

public interface BookingService {
    BookingResponseDto createBooking(Long userId, BookingDto bookingDto);

    BookingResponseDto updateBookingStatus(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getBookingById(Long userId, Long bookingId);

    List<BookingResponseDto> getBookingsByUser(Long userId, String state, int from, int size);

    List<BookingResponseDto> getBookingsByOwner(Long userId, String state, int from, int size);
}
