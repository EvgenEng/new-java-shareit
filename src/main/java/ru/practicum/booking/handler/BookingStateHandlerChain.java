package ru.practicum.booking.handler;

import org.springframework.stereotype.Component;
import ru.practicum.exception.UnsupportedStatusException;
import java.util.List;

@Component
public class BookingStateHandlerChain {
    private final List<BookingStateHandler> handlers;

    public BookingStateHandlerChain(List<BookingStateHandler> handlers) {
        this.handlers = handlers;
    }

    public BookingStateHandler getHandler(String state) {
        return handlers.stream()
                .filter(h -> h.canHandle(state.toUpperCase()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedStatusException("Unknown state: " + state));
    }
}
