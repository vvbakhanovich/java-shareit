package ru.practicum.shareit.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.validation.ConstraintViolationException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GatewayExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleConversionFailedException(MethodArgumentTypeMismatchException e) {
        ErrorMessage errorMessage = new ErrorMessage("Unknown state: " + e.getValue());
        log.error(e.getLocalizedMessage());
        return errorMessage;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidException(MethodArgumentNotValidException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        Map<String, String> exceptions = errorResponse.getErrors();
        for (ObjectError oe : e.getBindingResult().getAllErrors()) {
            exceptions.put(oe.getObjectName(), oe.getDefaultMessage());
            log.error("Объект {} не прошло валидацию. Причина: {}.", oe.getObjectName(), oe.getDefaultMessage());
        }
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            exceptions.put(error.getField(), error.getDefaultMessage());
            log.error("Поле {} не прошло валидацию. Причина: {}.", error.getField(), error.getDefaultMessage());
        }

        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemUnavailableException(ConstraintViolationException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put(e.getHeaderName(), e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put(e.getParameterName(), e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(HttpStatusCodeException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getResponseBodyAsString());
        log.error(e.getLocalizedMessage());
        return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        errorResponse.getErrors().put("stackTrace", getStackTraceAsString(e));
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    private String getStackTraceAsString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}
