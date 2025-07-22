package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AllBookingStateHandler extends AbstractBookingStateHandler {
    public AllBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "ALL");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable);
    }
}
