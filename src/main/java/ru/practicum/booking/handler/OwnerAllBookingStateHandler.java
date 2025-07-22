package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OwnerAllBookingStateHandler extends AbstractBookingStateHandler {
    public OwnerAllBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "OWNER_ALL");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByItemOwnerIdOrderByStartDesc(userId, pageable);
    }
}
