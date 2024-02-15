package ru.practicum.shareit.shared;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class LongIdGenerator implements IdGenerator<Long> {

    private Long initialId = 1L;

    @Override
    public Long generateId() {
        return initialId++;
    }
}
