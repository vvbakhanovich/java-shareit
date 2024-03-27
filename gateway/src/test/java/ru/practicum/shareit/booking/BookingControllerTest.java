package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.GetBookingState;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @MockBean
    private BookingClient bookingClient;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private long userId;

    private String header;

    private AddBookingDto addBookingDto;



    @BeforeEach
    void setUp() {
        userId = 1;
        header = "X-Sharer-User-Id";
        addBookingDto = AddBookingDto.builder()
                .itemId(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(5))
                .build();
    }

    @Test
    @DisplayName("Добавление бронирования, дата старта в прошлом")
    @SneakyThrows
    void addNewBooking_BookingStartInPast_ShouldThrowMethodArgumentNotValidException() {
        addBookingDto.setStart(LocalDateTime.now().minusDays(1));

        mvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException))
                .andExpect(jsonPath("$.errors.addBookingDto", is("Задан некорректный интервал бронирования.")));

        verify(bookingClient, never()).addBooking(anyLong(), any(AddBookingDto.class));
    }

    @Test
    @DisplayName("Добавление бронирования, запрос без заголовка")
    @SneakyThrows
    void addNewBooking_WithoutHeader_ShouldThrowMissingRequestHeaderException() {

        mvc.perform(post("/bookings")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBookingDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingClient, never()).addBooking(anyLong(), any(AddBookingDto.class));
    }

    @Test
    @DisplayName("Подтверждение бронирования, запрос без заголовка")
    @SneakyThrows
    void acknowledgeBooking_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        Long bookingId = 2L;
        Boolean approved = true;


        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .param("approved", approved.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingClient, never()).acknowledgeBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Подтверждение бронирования, запрос без статуса подтверждения")
    @SneakyThrows
    void acknowledgeBooking_WithoutApproved_ShouldThrowMissingServletRequestParameterException() {
        Long bookingId = 2L;

        mvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header(header, userId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException()
                        instanceof MissingServletRequestParameterException));

        verify(bookingClient, never()).acknowledgeBooking(anyLong(), anyLong(), anyBoolean());
    }

    @Test
    @DisplayName("Поиск бронирования по id, запрос без заголовка")
    @SneakyThrows
    void getBookingById_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        Long bookingId = 2L;

        mvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingClient, never()).getBooking(anyLong(), any());
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя, запрос без заголовка")
    @SneakyThrows
    void getAllBookingsFromUser_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        GetBookingState state = GetBookingState.FUTURE;
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings")
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingClient, never()).getBookings(anyLong(), any(GetBookingState.class), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Поиск всех бронирований пользователя, неизвестный статус")
    @SneakyThrows
    void getAllBookingsFromUser_UnknownState_ShouldThrowMethodArgumentTypeMismatchException() {
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("state", "TEST")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));

        verify(bookingClient, never()).getBookings(anyLong(), any(GetBookingState.class), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Поиск всех бронирований вещей пользователя, запрос без заголовка")
    @SneakyThrows
    void getAllOwnerBookings_WithoutHeader_ShouldThrowMissingRequestHeaderException() {
        GetBookingState state = GetBookingState.FUTURE;
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings/owner")
                        .param("state", state.name())
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(GetBookingState.class), anyLong(), anyInt());
    }

    @Test
    @DisplayName("Поиск всех бронирований вещей пользователя, неизвестный статус")
    @SneakyThrows
    void getAllOwnerBookings_UnknownState_ShouldThrowMethodArgumentTypeMismatchException() {
        Long from = 1L;
        Integer size = 5;

        mvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param("state", "TEST")
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));

        verify(bookingClient, never()).getOwnerBookings(anyLong(), any(GetBookingState.class), anyLong(), anyInt());
    }
}