package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).addUser(userDto);
    }

    @Test
    @SneakyThrows
    void addUser_NotValidUser_ShouldThrowMethodArgumentNotValidException() {
        userDto.setEmail(null);

        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.email", is("Должен быть обязательно указан email.")));

        verify(userService, never()).addUser(userDto);
    }

    @Test
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
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).updateUser(userId, updateDto);
    }

    @Test
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
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService, times(1)).findUserById(userId);
    }

    @Test
    @SneakyThrows
    void getAllUsers_ShouldReturnListOfUserDto() {
        when(userService.findAllUsers())
                .thenReturn(List.of(userDto));

        mvc.perform(get("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(userDto))))
                .andExpect(jsonPath("$.[0].email", is(userDto.getEmail())))
                .andExpect(jsonPath("$.length()", is(1)));

        verify(userService, times(1)).findAllUsers();
    }

    @Test
    @SneakyThrows
    void deleteUserById_ShouldReturnOkStatus() {
        mvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).deleteUserById(userId);
    }
}