package ru.practicum.item;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.practicum.item.dto.CommentDto;
import ru.practicum.user.User;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "authorName", source = "author", qualifiedByName = "mapAuthorName")
    CommentDto toDto(Comment comment);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "created", ignore = true)
    Comment toEntity(CommentDto commentDto);

    @Named("mapAuthorName")
    default String mapAuthorName(User author) {
        if (author == null) {
            return null;
        }
        return author.getName();
    }
}
