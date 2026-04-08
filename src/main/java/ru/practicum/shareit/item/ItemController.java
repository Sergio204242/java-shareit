package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Validated
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemResponseDto createItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId,
                                      @Valid @RequestBody ItemDto itemDto) {

        return itemService.createItem(itemDto, userId);
    }

    @PatchMapping("/{id}")
    public ItemResponseDto updateItem(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId,
                                      @NotNull @Positive @PathVariable Long id,
                                      @RequestBody ItemUpdateDto itemDto) {

        return itemService.updateItem(itemDto, id, userId);
    }

    @GetMapping("/{id}")
    public ItemResponseDto getItem(@NotNull @Positive @PathVariable Long id) {
        return itemService.getItem(id);
    }

    @GetMapping
    public List<ItemResponseDto> getItems(@NotNull @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/search")
    public List<ItemResponseDto> searchItem(@NotNull @RequestParam String text) {
        return itemService.searchItem(text);
    }
}