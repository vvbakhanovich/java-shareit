package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ItemDto {

    private Long id;

    @NotBlank(message = "Название вещи не может быть пустым.")
    private String name;

    @NotBlank(message = "Описание вещи не может быть пустым.")
    private String description;

    @NotNull(message = "У вещи обязательно должен быть указан статус доступности.")
    private Boolean available;
}
