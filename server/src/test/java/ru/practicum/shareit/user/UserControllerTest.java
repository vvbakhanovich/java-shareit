package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;

    private long userId;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("name")
                .email("test@mail.com")
                .build();
        userId = 1;
    }

    @Test
    @DisplayName("Добавление пользователя")
    @SneakyThrows
    void addUser_ValidUser_ShouldReturnDto() {
        when(userService.addUser(userDto))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).addUser(userDto);
    }

    @Test
    @DisplayName("Обновление данных пользователя")
    @SneakyThrows
    void updateUser_ShouldReturnUserDto() {
        UserUpdateDto updateDto = new UserUpdateDto();
        when(userService.updateUser(userId, updateDto))
                .thenReturn(userDto);

        mvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).updateUser(userId, updateDto);
    }

    @Test
    @DisplayName("Поиск пользователя по id")
    @SneakyThrows
    void getUserById_shouldReturnUserDto() {
        when(userService.findUserById(userId))
                .thenReturn(userDto);

        mvc.perform(get("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(userDto)))
                .andExpect(jsonPath("$.id", is(userDto.getId())))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).findUserById(userId);
    }

    @Test
    @DisplayName("Поиск всех пользователей")
    @SneakyThrows
    void getAllUsers_ShouldReturnListOfUserDto() {
        when(userService.findAllUsers())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(userDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(userDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @DisplayName("Удаление пользователя по id")
    @SneakyThrows
    void deleteUserById_ShouldReturnOkStatus() {
        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUserById(userId);
    }
}