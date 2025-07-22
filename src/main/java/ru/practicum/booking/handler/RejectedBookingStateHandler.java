package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.Booking.BookingStatus;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class RejectedBookingStateHandler extends AbstractBookingStateHandler {
    public RejectedBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "REJECTED");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                userId, BookingStatus.REJECTED, pageable);
    }
}
