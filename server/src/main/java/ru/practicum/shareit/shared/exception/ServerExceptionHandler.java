package ru.practicum.shareit.shared.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ServerExceptionHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(NotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleItemUnavailableException(ItemUnavailableException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotAuthorizedException(NotAuthorizedException e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleAllException(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.getErrors().put("errorMessage", e.getLocalizedMessage());
        log.error(e.getLocalizedMessage());
        return errorResponse;
    }
}
