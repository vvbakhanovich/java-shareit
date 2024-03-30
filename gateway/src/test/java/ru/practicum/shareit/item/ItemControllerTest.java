package ru.practicum.shareit.item;

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
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemClient itemClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long userId;

    private String header;

    private ItemDto itemDto;

    private long itemId;

    @BeforeEach
    void setUp() {
        userId = 1;
        header = "X-Sharer-User-Id";
        itemDto = ItemDto.builder()
                .name("name")
                .available(true)
                .description("description")
                .build();
        itemId = 2;
    }

    @Test
    @DisplayName("Добавление вещи, запрос без заголовка")
    @SneakyThrows
    void addItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).addItem(any(), any());
    }

    @Test
    @DisplayName("Добавление вещи с невалидными полями")
    @SneakyThrows
    void addItem_ItemDtoNotValid_ShouldThrowMethodArgumentNotValidExceptionAndStatus400() {
        itemDto.setAvailable(null);

        mvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

        verify(itemClient, never()).addItem(any(), any());
    }

    @Test
    @DisplayName("Обновление данных о вещи, запрос без заголовка")
    @SneakyThrows
    void updateItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("updated name")
                .description("updated description")
                .available(false)
                .build();

        mvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).updateItem(anyLong(), anyLong(), any(ItemUpdateDto.class));
    }

    @Test
    @DisplayName("Получение вещи по id, запрос без заголовка")
    @SneakyThrows
    void getItemById_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).findItemById(anyLong(), anyLong());
    }

    @Test
    @DisplayName("Получение вещей пользователя")
    @SneakyThrows
    void getAllItemsByUserId_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).findAllItemsByUserId(any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, запрос без заголовка")
    @SneakyThrows
    void searchItems_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        String text = "search";

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).searchItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, пустой запрос")
    @SneakyThrows
    void searchItems_WithoutText_ShouldThrowMissingServletRequestParameterExceptionExceptionAndStatus400() {
        mvc.perform(get("/items/search")
                        .header(header, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof
                        MissingServletRequestParameterException));

        verify(itemClient, never()).searchItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, пустая строка")
    @SneakyThrows
    void searchItems_TextIsEmpty_ShouldRetrunEmptyCollection() {
        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        verify(itemClient, never()).searchItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, строка из пробелов")
    @SneakyThrows
    void searchItems_TextIsOnlyBlancs_ShouldRetrunEmptyCollection() {
        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()", is(0)));

        verify(itemClient, never()).searchItems(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Добавление комментария, запрос без заголовка")
    @SneakyThrows
    void addCommentToItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndReturnStatus400() {
        AddCommentDto addCommentDto = new AddCommentDto("comment");

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemClient, never()).addCommentToItem(any(), any(), any());
    }


    @Test
    @DisplayName("Добавление комментария, пустой комментарий")
    @SneakyThrows
    void addCommentToItem_AddCommentDtoNotValid_ShouldThrowMethodArgumentNotValidExceptionAndReturnStatus400() {
        AddCommentDto addCommentDto = new AddCommentDto(null);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

        verify(itemClient, never()).addCommentToItem(any(), any(), any());
    }
}