package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.booking.Booking;
import ru.practicum.booking.BookingRepository;
import ru.practicum.booking.Booking.BookingStatus;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OwnerWaitingBookingStateHandler extends AbstractBookingStateHandler {
    public OwnerWaitingBookingStateHandler(BookingRepository bookingRepository) {
        super(bookingRepository, "OWNER_WAITING");
    }

    @Override
    public List<Booking> handle(Long userId, Pageable pageable, LocalDateTime now) {
        return bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(
                userId, BookingStatus.WAITING, pageable);
    }
}
