package ru.practicum.shareit.shared;

public interface IdGenerator<T extends Number> {
    T generateId();
}
