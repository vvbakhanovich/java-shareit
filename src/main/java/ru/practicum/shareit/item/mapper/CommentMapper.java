package ru.practicum.shareit.item.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);

    @AfterMapping
    default void setAuthorName(Comment comment, @MappingTarget CommentDto commentDto) {
        commentDto.setAuthorName(comment.getAuthor().getName());
    }
}
