package ru.practicum.shareit.request;

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
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.request.dto.AddItemRequestDto;

import javax.validation.ConstraintViolationException;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestClient itemRequestClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String header;

    private Long userId;

    @BeforeEach
    public void setUp() {
        header = "X-Sharer-User-id";
        userId = 1L;
    }

    @Test
    @DisplayName("Добавление нового запроса, запрос без описания")
    @SneakyThrows
    public void addNewItemRequest_NotValidRequestBody_ShouldThrowMethodArgumentNotValidException() {
        AddItemRequestDto requestDto = new AddItemRequestDto();

        mvc.perform(post("/requests")
                        .header(header, userId)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.description", is("Описание не может быть пустым.")));

        verify(itemRequestClient, never()).addNewItemRequest(eq(userId), any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, from < 0")
    @SneakyThrows
    public void getAvailableItemRequests_NegativeFrom_ShouldThrowConstraintViolationException() {
        mvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param("from", "-1")
                        .param("size", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));

        verify(itemRequestClient, never()).getAvailableItemRequests(any(), any(), any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, size < 0")
    @SneakyThrows
    public void getAvailableItemRequests_NegativeSize_ShouldThrowConstraintViolationException() {
        mvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param("from", "1")
                        .param("size", "-14"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));

        verify(itemRequestClient, never()).getAvailableItemRequests(any(), any(), any());
    }

    @Test
    @DisplayName("Поиск доступных запросов, size = 0")
    @SneakyThrows
    public void getAvailableItemRequests_ZeroSize_ShouldThrowConstraintViolationException() {
        mvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param("from", "1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ConstraintViolationException));

        verify(itemRequestClient, never()).getAvailableItemRequests(any(), any(), any());
    }
}