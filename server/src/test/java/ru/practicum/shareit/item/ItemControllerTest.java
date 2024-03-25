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
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {

    @MockBean
    private ItemService itemService;

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
    @DisplayName("Добавление вещи")
    @SneakyThrows
    void addItem_ShouldReturnStatus201() {
        when(itemService.addItem(userId, itemDto))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(itemDto)))
                .andExpect(jsonPath("$.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).addItem(userId, itemDto);
    }

    @Test
    @DisplayName("Добавление вещи, запрос без заголовка")
    @SneakyThrows
    void addItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        when(itemService.addItem(userId, itemDto))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).addItem(any(), any());
    }

    @Test
    @DisplayName("Добавление вещи с невалидными полями")
    @SneakyThrows
    void addItem_ItemDtoNotValid_ShouldThrowMethodArgumentNotValidExceptionAndStatus400() {
        itemDto.setAvailable(null);
        when(itemService.addItem(userId, itemDto))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

        verify(itemService, never()).addItem(any(), any());
    }

    @Test
    @DisplayName("Обновление данных о вещи")
    @SneakyThrows
    void updateItem_ShouldReturnStatus200() {
        ItemUpdateDto itemUpdateDto = ItemUpdateDto.builder()
                .name("updated name")
                .description("updated description")
                .available(false)
                .build();
        when(itemService.updateItem(userId, itemId,itemUpdateDto))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(itemDto)))
                .andExpect(jsonPath("$.id", is(itemDto.getId())))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).updateItem(userId, itemId, itemUpdateDto);
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
        when(itemService.updateItem(userId, itemId,itemUpdateDto))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/{itemId}", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemUpdateDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).updateItem(any(),any(), any());
    }

    @Test
    @DisplayName("Получение вещи по id")
    @SneakyThrows
    void getItemById_ShouldReturnStatus200() {
        GetItemDto getItemDto = new GetItemDto();
        when(itemService.findItemById(userId, itemId))
                .thenReturn(getItemDto);

        mvc.perform(get("/items/{itemId}", itemId)
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(getItemDto)))
                .andExpect(jsonPath("$.id", is(getItemDto.getId())))
                .andExpect(jsonPath("$.name", is(getItemDto.getName())))
                .andExpect(jsonPath("$.description", is(getItemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(getItemDto.getAvailable())))
                .andExpect(jsonPath("$.lastBooking", is(getItemDto.getLastBooking())))
                .andExpect(jsonPath("$.nextBooking", is(getItemDto.getNextBooking())))
                .andExpect(jsonPath("$.comments", is(getItemDto.getComments())));

        verify(itemService, times(1)).findItemById(userId, itemId);
    }

    @Test
    @DisplayName("Получение вещи по id, запрос без заголовка")
    @SneakyThrows
    void getItemById_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        GetItemDto getItemDto = new GetItemDto();
        when(itemService.findItemById(userId, itemId))
                .thenReturn(getItemDto);

        mvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).findItemById(any(),any());
    }

    @Test
    @DisplayName("Получение вещей пользователя с параметрами по умолчанию")
    @SneakyThrows
    void getAllItemsByUserId_WithoutParams_ShouldReturnStatus200() {
        long from = 0L;
        int size = 10;
        GetItemDto getItemDto = new GetItemDto();
        when(itemService.findAllItemsByUserId(userId, from, size))
                .thenReturn(List.of(getItemDto));

        mvc.perform(get("/items")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(getItemDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(getItemDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(getItemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(getItemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(getItemDto.getAvailable())))
                .andExpect(jsonPath("$.[0].lastBooking", is(getItemDto.getLastBooking())))
                .andExpect(jsonPath("$.[0].nextBooking", is(getItemDto.getNextBooking())))
                .andExpect(jsonPath("$.[0].comments", is(getItemDto.getComments())));

        verify(itemService, times(1)).findAllItemsByUserId(userId, from, size);
    }

    @Test
    @DisplayName("Получение вещей пользователя")
    @SneakyThrows
    void getAllItemsByUserId_WithParams_ShouldReturnStatus200() {
        long from = 1;
        int size = 5;
        GetItemDto getItemDto = new GetItemDto();
        when(itemService.findAllItemsByUserId(userId, from, size))
                .thenReturn(List.of(getItemDto));

        mvc.perform(get("/items")
                        .header(header, userId)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(getItemDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(getItemDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(getItemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(getItemDto.getDescription())))
                .andExpect(jsonPath("$.[0].available", is(getItemDto.getAvailable())))
                .andExpect(jsonPath("$.[0].lastBooking", is(getItemDto.getLastBooking())))
                .andExpect(jsonPath("$.[0].nextBooking", is(getItemDto.getNextBooking())))
                .andExpect(jsonPath("$.[0].comments", is(getItemDto.getComments())));

        verify(itemService, times(1)).findAllItemsByUserId(userId, from, size);
    }

    @Test
    @DisplayName("Получение вещей пользователя")
    @SneakyThrows
    void getAllItemsByUserId_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        long from = 1;
        int size = 4;
        GetItemDto getItemDto = new GetItemDto();
        when(itemService.findAllItemsByUserId(userId, from, size))
                .thenReturn(List.of(getItemDto));

        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).findAllItemsByUserId(any(), eq(from), eq(size));
    }

    @Test
    @DisplayName("Поиск вещей, запрос без параметров")
    @SneakyThrows
    void searchItems_WithoutParams_ShouldReturnStatus200() {
        String text = "search";
        long from = 0;
        int size = 10;
        when(itemService.searchItems(text, from, size))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(itemDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].requestId", is(itemDto.getRequestId())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).searchItems(text, from, size);
    }

    @Test
    @DisplayName("Поиск вещей")
    @SneakyThrows
    void searchItems_WithParams_ShouldReturnStatus200() {
        String text = "search";
        long from = 1;
        int size = 5;
        when(itemService.searchItems(text, from, size))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header(header, userId)
                        .param("text", text)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(itemDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemDto.getId())))
                .andExpect(jsonPath("$.[0].name", is(itemDto.getName())))
                .andExpect(jsonPath("$.[0].description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.[0].requestId", is(itemDto.getRequestId())))
                .andExpect(jsonPath("$.[0].available", is(itemDto.getAvailable())));

        verify(itemService, times(1)).searchItems(text, from, size);
    }

    @Test
    @DisplayName("Поиск вещей, запрос без заголовка")
    @SneakyThrows
    void searchItems_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndStatus400() {
        long from = 0;
        int size = 10;
        String text = "search";
        when(itemService.searchItems(text, from, size))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .param("text", text))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).searchItems(any(), any(), any());
    }

    @Test
    @DisplayName("Поиск вещей, пустой запрос")
    @SneakyThrows
    void searchItems_WithoutText_ShouldThrowMissingServletRequestParameterExceptionExceptionAndStatus400() {
        long from = 0;
        int size = 10;
        String text = "search";
        when(itemService.searchItems(text, from, size))
                .thenReturn(List.of(itemDto));

        mvc.perform(get("/items/search")
                        .header(header, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof
                        MissingServletRequestParameterException));

        verify(itemService, never()).searchItems(any(), any(), any());
    }

    @Test
    @DisplayName("Добавление комментария")
    @SneakyThrows
    void addCommentToItem_ShouldReturnStatus201() {
        AddCommentDto addCommentDto = new AddCommentDto("comment");
        CommentDto commentDto = new CommentDto();
        when(itemService.addCommentToItem(userId, itemId, addCommentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(commentDto)))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.created", is(commentDto.getCreated())));

        verify(itemService, times(1)).addCommentToItem(userId, itemId, addCommentDto);
    }

    @Test
    @DisplayName("Добавление комментария, запрос без заголовка")
    @SneakyThrows
    void addCommentToItem_WithoutHeader_ShouldThrowMissingRequestHeaderExceptionAndReturnStatus400() {
        AddCommentDto addCommentDto = new AddCommentDto("comment");
        CommentDto commentDto = new CommentDto();
        when(itemService.addCommentToItem(userId, itemId, addCommentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemService, never()).addCommentToItem(any(), any(), any());
    }

    @Test
    @DisplayName("Добавление комментария, пустой комментарий")
    @SneakyThrows
    void addCommentToItem_AddCommentDtoNotValid_ShouldThrowMethodArgumentNotValidExceptionAndReturnStatus400() {
        AddCommentDto addCommentDto = new AddCommentDto(null);
        CommentDto commentDto = new CommentDto();
        when(itemService.addCommentToItem(userId, itemId, addCommentDto))
                .thenReturn(commentDto);

        mvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addCommentDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));

        verify(itemService, never()).addCommentToItem(any(), any(), any());
    }
}