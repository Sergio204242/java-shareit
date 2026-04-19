package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.request.CommentDto;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.CommentResponseDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponseDto createItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @Valid @RequestBody ItemDto itemDto) {

        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemResponseDto updateItem(@RequestHeader(USER_ID_HEADER) Long userId,
                                      @NotNull @Positive @PathVariable Long id,
                                      @RequestBody ItemUpdateDto itemDto) {

        return itemService.updateItem(itemDto, id, userId);
    }

    @GetMapping("/{id}")
    public ItemResponseDto getItem(@NotNull @Positive @PathVariable Long id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public List<ItemResponseDto> getItems(@RequestHeader(USER_ID_HEADER) Long userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItem(@NotNull @RequestParam String text) {
        return itemService.searchItem(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(@RequestHeader(USER_ID_HEADER) Long userId,
                                         @NotNull @Positive @PathVariable Long itemId,
                                         @Valid @RequestBody CommentDto commentDto) {
        return itemService.addComment(userId, itemId, commentDto);
    }
}