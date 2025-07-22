package ru.practicum.shareit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingResponseDto bookingResponseDto;
    private final long userId = 1L;
    private final long bookingId = 1L;

    @BeforeEach
    void setUp() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        bookingDto = BookingDto.builder()
                .itemId(1L)
                .start(start)
                .end(end)
                .build();

        BookingResponseDto.UserDto userDto = BookingResponseDto.UserDto.builder()
                .id(1L)
                .name("John Doe")
                .build();

        BookingResponseDto.ItemDto itemDto = BookingResponseDto.ItemDto.builder()
                .id(1L)
                .name("Drill")
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .status(BookingStatus.WAITING)
                .booker(userDto)
                .item(itemDto)
                .build();
    }

    @Test
    void shouldCreateBooking() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.booker.id").value(1L))
                .andExpect(jsonPath("$.item.id").value(1L));
    }

    @Test
    void shouldNotCreateBookingWithInvalidDates() throws Exception {
        when(bookingService.createBooking(anyLong(), any(BookingDto.class)))
                .thenThrow(new ValidationException("Start date must be before end date"));

        mockMvc.perform(post("/bookings")
                .header("X-Sharer-User-Id", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldApproveBooking() throws Exception {
        BookingResponseDto approvedBooking = BookingResponseDto.builder()
                .id(1L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.APPROVED)
                .booker(bookingResponseDto.getBooker())
                .item(bookingResponseDto.getItem())
                .build();

        when(bookingService.updateBookingStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(approvedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                .header("X-Sharer-User-Id", userId)
                .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void shouldRejectBooking() throws Exception {
        BookingResponseDto rejectedBooking = BookingResponseDto.builder()
                .id(1L)
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(BookingStatus.REJECTED)
                .booker(bookingResponseDto.getBooker())
                .item(bookingResponseDto.getItem())
                .build();

        when(bookingService.updateBookingStatus(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                .header("X-Sharer-User-Id", userId)
                .param("approved", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void shouldGetBookingById() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void shouldGetBookingByIdNotFound() throws Exception {
        when(bookingService.getBookingById(anyLong(), anyLong()))
                .thenThrow(new NotFoundException("Booking not found"));

        mockMvc.perform(get("/bookings/{bookingId}", 99L)
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetUserBookings() throws Exception {
        when(bookingService.getBookingsByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", userId)
                .param("state", "ALL")
                .param("from", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldGetOwnerBookings() throws Exception {
        when(bookingService.getBookingsByOwner(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings/owner")
                .header("X-Sharer-User-Id", userId)
                .param("state", "ALL")
                .param("from", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void shouldGetUserBookingsWithDefaultParameters() throws Exception {
        when(bookingService.getBookingsByUser(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(bookingResponseDto));

        mockMvc.perform(get("/bookings")
                .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk());
    }
}
