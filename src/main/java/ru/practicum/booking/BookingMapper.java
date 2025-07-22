package ru.practicum.booking;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.booking.dto.BookingDto;
import ru.practicum.booking.dto.BookingResponseDto;
import ru.practicum.item.Item;
import ru.practicum.item.ItemMapper;
import ru.practicum.user.User;
import ru.practicum.user.UserMapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ItemMapper.class})
public interface BookingMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "start", source = "start")
    @Mapping(target = "end", source = "end")
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "status", source = "status")
    BookingResponseDto toResponseDto(Booking booking);

    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    BookingDto toDto(Booking booking);

    @Mapping(target = "item", source = "itemId", qualifiedByName = "idToItem")
    @Mapping(target = "booker", source = "bookerId", qualifiedByName = "idToUser")
    @Mapping(target = "status", ignore = true)
    Booking toEntity(BookingDto bookingDto);

    @Named("idToItem")
    default Item idToItem(Long id) {
        if (id == null) {
            return null;
        }
        Item item = new Item();
        item.setId(id);
        return item;
    }

    @Named("idToUser")
    default User idToUser(Long id) {
        if (id == null) {
            return null;
        }
        User user = new User();
        user.setId(id);
        return user;
    }
}
