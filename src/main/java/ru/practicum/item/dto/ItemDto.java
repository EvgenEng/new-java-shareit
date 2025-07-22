package ru.practicum.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private Boolean available;

    private BookingShort lastBooking;
    private BookingShort nextBooking;
    private List<CommentDto> comments = new ArrayList<>();
    private Long requestId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingShort {
        private Long id;
        private Long bookerId;
    }
}
