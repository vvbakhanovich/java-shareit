package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private long id;
    @NotBlank(message = "Должно быть указано имя пользователя.")
    private String name;
    @NotBlank(message = "Должен быть email.")
    private String email;
}
