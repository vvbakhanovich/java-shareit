package ru.practicum.shareit.shared.exception;

public class EmailAlreadyExists extends RuntimeException {

    public EmailAlreadyExists() {
        super();
    }

    public EmailAlreadyExists(String message) {
        super(message);
    }
}
