package ru.practicum.booking.handler;

import ru.practicum.booking.Booking;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingStateHandler {

    boolean canHandle(String state);

    List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now);
}
