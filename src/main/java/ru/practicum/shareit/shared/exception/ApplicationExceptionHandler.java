package ru.practicum.shareit.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ApplicationExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleEmailAlreadyExistsException(EmailAlreadyExists e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemUnavailableException(ItemUnavailableException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("Error message", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotAuthorizedException(NotAuthorizedException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("ErrorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage handleConversionFailedException(MethodArgumentTypeMismatchException e) {
        ErrorMessage errorMessage = new ErrorMessage("Unknown state: UNSUPPORTED_STATUS");
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }
}
