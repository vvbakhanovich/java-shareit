package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String DEFAULT_PAGE_SIZE = "10";

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto addItem(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        return itemService.addItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable long itemId,
                              @RequestBody ItemUpdateDto itemUpdateDto) {
        return itemService.updateItem(userId, itemId, itemUpdateDto);
    }

    @GetMapping("/{itemId}")
    public GetItemDto getItemById(@RequestHeader("X-Sharer-User-Id") Long userId, @PathVariable long itemId) {
        return itemService.findItemById(userId, itemId);
    }

    @GetMapping
    public List<GetItemDto> getAllItemsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @RequestParam(defaultValue = "0") Long from,
                                                @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        return itemService.findAllItemsByUserId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestParam String text,
                                     @RequestParam(defaultValue = "0") Long from,
                                     @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) Integer size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addCommentToItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                       @PathVariable Long itemId,
                                       @RequestBody AddCommentDto commentDto) {
        return itemService.addCommentToItem(userId, itemId, commentDto);
    }
}
