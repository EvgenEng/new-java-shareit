package ru.practicum.shareit.item.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemWithBookingDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;
    private List<CommentDto> comments;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BookingShortDto {
        private Long id;
        private Long bookerId;
    }
}
