package ru.practicum.booking.handler;

import ru.practicum.booking.BookingRepository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractBookingStateHandler implements BookingStateHandler {
    protected final BookingRepository bookingRepository;
    protected final String supportedState;

    protected AbstractBookingStateHandler(BookingRepository bookingRepository, String supportedState) {
        this.bookingRepository = bookingRepository;
        this.supportedState = supportedState;
    }

    @Override
    public boolean canHandle(String state) {
        return supportedState.equalsIgnoreCase(state);
    }
}
