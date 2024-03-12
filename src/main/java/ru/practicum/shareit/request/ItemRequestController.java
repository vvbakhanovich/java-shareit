package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.AddItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addNewItemRequest(@RequestHeader("X-Sharer-User-id") Long userId,
                                            @RequestBody @Valid AddItemRequestDto addItemRequestDto) {
        return itemRequestService.addNewItemRequest(userId, addItemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAllItemRequestsFromUser(@RequestHeader("X-Sharer-User-id") Long userId) {
        return itemRequestService.getAllItemRequestsFromUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(@RequestHeader("X-Sharer-User-id") Long userId,
                                                   @RequestParam(required = false) @PositiveOrZero Long from,
                                                   @RequestParam(required = false) @Positive Integer size) {
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequestById(@RequestHeader("X-Sharer-User-id") Long userId,
                                             @PathVariable Long requestId) {
        return itemRequestService.getItemRequestById(userId, requestId);
    }
}
