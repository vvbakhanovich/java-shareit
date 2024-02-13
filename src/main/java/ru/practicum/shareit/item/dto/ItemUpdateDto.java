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
public class ItemUpdateDto {
    @NotBlank(message = "Название не может быть пустым.")
    private String name;
    private String description;
    @NotNull(message = "Не указан статус доступности.")
    private Boolean available;
}
