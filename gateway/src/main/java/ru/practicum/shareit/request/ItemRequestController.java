package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static ru.practicum.shareit.shared.ControllerConstants.DEFAULT_PAGE_SIZE;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ItemRequestDto addNewItemRequest(@RequestHeader("X-Sharer-User-id") long userId,
                                            @RequestBody @Valid AddItemRequestDto addItemRequestDto) {
        return itemRequestClient.addNewItemRequest(userId, addItemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllItemRequestsFromUser(@RequestHeader("X-Sharer-User-id") long userId) {
        return itemRequestClient.getAllItemRequestsFromUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAvailableItemRequests(@RequestHeader("X-Sharer-User-id") long userId,
                                                         @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
                                                         @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) @Positive Integer size) {
        return itemRequestClient.getAvailableItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-id") long userId,
                                             @PathVariable Long requestId) {
        return itemRequestClient.getItemRequestById(userId, requestId);
    }
}
