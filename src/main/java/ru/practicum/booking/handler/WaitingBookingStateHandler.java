package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.Booking.BookingStatus;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class WaitingBookingStateHandler extends AbstractBookingStateHandler {
    public WaitingBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "WAITING");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                userId, BookingStatus.WAITING, pageable);
    }
}
