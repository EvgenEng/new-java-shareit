package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class FutureBookingStateHandler extends AbstractBookingStateHandler {
    public FutureBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "FUTURE");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now, pageable);
    }
}
