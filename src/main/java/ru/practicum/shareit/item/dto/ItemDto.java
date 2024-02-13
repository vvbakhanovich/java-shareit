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
    private long id;
    @NotBlank(message = "У вещи должно быть название.")
    private String name;
    @NotBlank(message = "У вещи должно быть описание.")
    private String description;
    @NotNull(message = "У вещи должен быть указан статус доступности.")
    private Boolean available;
}
