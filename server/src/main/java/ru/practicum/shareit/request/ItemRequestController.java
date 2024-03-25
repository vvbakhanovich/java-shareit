package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemRequestDto addNewItemRequest(@RequestHeader("X-Sharer-User-id") long userId,
                                            @RequestBody AddItemRequestDto addItemRequestDto) {
        return itemRequestService.addNewItemRequest(userId, addItemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllItemRequestsFromUser(@RequestHeader("X-Sharer-User-id") long userId) {
        return itemRequestService.getAllItemRequestsFromUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAvailableItemRequests(@RequestHeader("X-Sharer-User-id") long userId,
                                                         @RequestParam Long from,
                                                         @RequestParam Integer size) {
        return itemRequestService.getAvailableItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-id") long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
