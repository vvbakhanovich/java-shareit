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
import org.springframework.web.bind.MissingRequestHeaderException;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @MockBean
    private ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AddItemRequestDto addItemRequestDto;

    private ItemRequestDto itemRequestDto;

    private String header;

    private Long userId;

    @BeforeEach
    public void setUp() {
        addItemRequestDto = new AddItemRequestDto("description");
        itemRequestDto = new ItemRequestDto();
        header = "X-Sharer-User-id";
        userId = 1L;
    }

    @Test
    @DisplayName("Добавление нового запроса,запрос без заголовка")
    @SneakyThrows
    public void addNewItemRequest_WithoutHeader_ShouldThrowMissingRequestHeaderException() {

        mvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(addItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemRequestService, never()).addNewItemRequest(any(), any());
    }

    @Test
    @DisplayName("Добавление нового запроса,запрос без заголовка")
    @SneakyThrows
    public void addNewItemRequest_ValidRequest_ShouldReturnRequest() {
        when(itemRequestService.addNewItemRequest(userId, addItemRequestDto))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .header(header, 1)
                        .content(objectMapper.writeValueAsString(addItemRequestDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated())))
                .andExpect(jsonPath("$.items", is(itemRequestDto.getItems())));
        verify(itemRequestService, times(1)).addNewItemRequest(userId, addItemRequestDto);
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя, запрос без заголовка")
    @SneakyThrows
    void getAllItemRequestsFromUser_NoHeader_ShouldThrowMissingRequestHeaderException() {
        mvc.perform(get("/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemRequestService, never()).addNewItemRequest(any(), any());
    }

    @Test
    @DisplayName("Поиск всех запросов пользователя")
    @SneakyThrows
    public void getAllItemRequestsFromUser_Valid_ShouldReturnRequest() {
        when(itemRequestService.getAllItemRequestsFromUser(userId))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests")
                        .header(header, userId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(itemRequestDto))))
                .andExpect(jsonPath("$.length()", is(1)))
                .andExpect(jsonPath("$.[0].id", is(itemRequestDto.getId())))
                .andExpect(jsonPath("$.[0].description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.[0].created", is(itemRequestDto.getCreated())))
                .andExpect(jsonPath("$.[0].items", is(itemRequestDto.getItems())));

        verify(itemRequestService, times(1)).getAllItemRequestsFromUser(userId);
    }

    @Test
    @DisplayName("Поиск доступных запросов")
    @SneakyThrows
    public void getAvailableItemRequests_WithAllNotNullFields_ShouldThrowConstraintViolationException() {
        when(itemRequestService.getAvailableItemRequests(userId, 1L, 2))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header(header, userId)
                        .param("from", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(List.of(itemRequestDto))));

        verify(itemRequestService, times(1)).getAvailableItemRequests(userId, 1L, 2);
    }

    @Test
    @DisplayName("Поиск запроса по id, запрос без заголовка")
    @SneakyThrows
    public void getItemRequestById_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        mvc.perform(get("/requests/{requestId}", 1))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(itemRequestService, never()).getItemRequestById(any(), any());
    }

    @Test
    @DisplayName("Поиск запроса по id")
    @SneakyThrows
    public void getItemRequestById_WithRequestId_ShouldReturnRequest() {
        long requestId = 2;
        when(itemRequestService.getItemRequestById(userId, requestId))
                .thenReturn(itemRequestDto);

        mvc.perform(get("/requests/{requestId}", String.valueOf(requestId))
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId())))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription())))
                .andExpect(jsonPath("$.created", is(itemRequestDto.getCreated())))
                .andExpect(jsonPath("$.items", is(itemRequestDto.getItems())));

        verify(itemRequestService, times(1)).getItemRequestById(userId, requestId);
    }
}