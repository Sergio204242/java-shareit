package ru.practicum.shareit;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;

    private long ownerId;

    @BeforeEach
    void setUp() {
        ownerId = userService.create(createUserDto("owner@example.com", "Owner")).getId();
    }

    @Test
    @DisplayName("createItem: создание item")
    void createItem() {
        ItemResponseDto result = itemService.createItem(itemDto("Дрель", "Электрическая", true), ownerId);

        assertTrue(result.getId() > 0);
        assertEquals("Дрель", result.getName());
        assertEquals("Электрическая", result.getDescription());
        assertTrue(result.getAvailable());
    }

    @Test
    @DisplayName("createItem: несуществующий userId")
    void createItemUnknownUser() {
        assertThrows(UserNotFoundException.class,
                () -> itemService.createItem(itemDto("Лопата", "Штыковая", true), 999_999L));
    }

    @Test
    @DisplayName("createItem: два разных пользователя создают item")
    void createItemTwoOwners() {
        long owner2 = userService.create(createUserDto("owner2@example.com", "Owner2")).getId();

        itemService.createItem(itemDto("Вещь1", "Описание1", true), ownerId);
        itemService.createItem(itemDto("Вещь2", "Описание2", true), owner2);

        assertEquals(1, itemService.getItems(ownerId).size());
        assertEquals(1, itemService.getItems(owner2).size());
    }

    @Test
    @DisplayName("updateItem: обновление названия, описание не меняется")
    void updateItemName() {
        ItemResponseDto created = itemService.createItem(itemDto("Старое", "Описание", true), ownerId);
        ItemResponseDto updated = itemService.updateItem(updateDto("Новое", null, null), created.getId(), ownerId);

        assertEquals("Новое", updated.getName());
        assertEquals("Описание", updated.getDescription());
    }

    @Test
    @DisplayName("updateItem: обновление описания и доступности")
    void updateItemDescriptionAndAvailable() {
        ItemResponseDto created = itemService.createItem(itemDto("Молоток", "Старое", true), ownerId);
        ItemResponseDto updated = itemService.updateItem(updateDto(null, "Новое", false), created.getId(), ownerId);

        assertEquals("Новое", updated.getDescription());
        assertFalse(updated.getAvailable());
    }

    @Test
    @DisplayName("updateItem: чужой userId")
    void updateItemWrongOwner() {
        ItemResponseDto created = itemService.createItem(itemDto("Вещь", "Описание", true), ownerId);
        long stranger = userService.create(createUserDto("stranger@example.com", "Stranger")).getId();

        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(updateDto("Чужое", null, null), created.getId(), stranger));
    }

    @Test
    @DisplayName("updateItem: несуществующий itemId")
    void updateItemUnknownItem() {
        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(updateDto("X", null, null), 999_999L, ownerId));
    }

    @Test
    @DisplayName("getItem: возвращает нужную вещь")
    void getItem() {
        ItemResponseDto created = itemService.createItem(itemDto("Книга", "Интересная", true), ownerId);
        ItemResponseDto found = itemService.getItem(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Книга", found.getName());
    }

    @Test
    @DisplayName("getItem: несуществующий id")
    void getItemUnknownId() {
        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(999_999L));
    }

    @Test
    @DisplayName("getItems: возвращает только вещи данного владельца")
    void getItemsReturnsOnlyOwnerItems() {
        long owner2 = userService.create(createUserDto("o2@example.com", "O2")).getId();

        itemService.createItem(itemDto("Вещь А", "Описание А", true), ownerId);
        itemService.createItem(itemDto("Вещь Б", "Описание Б", true), ownerId);
        itemService.createItem(itemDto("Чужая", "Чужое", true), owner2);

        List<ItemResponseDto> items = itemService.getItems(ownerId);

        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(i ->
                i.getName().equals("Вещь А") || i.getName().equals("Вещь Б")));
    }

    @Test
    @DisplayName("getItems: у пользователя нет вещей")
    void getItemsNoItems() {
        assertTrue(itemService.getItems(ownerId).isEmpty());
    }

    @Test
    @DisplayName("searchItem: поиск по части названия")
    void searchItemByName() {
        itemService.createItem(itemDto("Электродрель", "Мощная дрель", true), ownerId);
        itemService.createItem(itemDto("Перфоратор", "Хороший перфоратор", true), ownerId);

        List<ItemResponseDto> result = itemService.searchItem("дрель");

        assertEquals(1, result.size());
        assertEquals("Электродрель", result.get(0).getName());
    }

    @Test
    @DisplayName("searchItem: поиск по части описания")
    void searchItemByDescription() {
        itemService.createItem(itemDto("Инструмент", "гаечный ключ", true), ownerId);

        List<ItemResponseDto> result = itemService.searchItem("гаечный");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchItem: недоступные вещи не попадают в результат")
    void searchItemUnavailable() {
        itemService.createItem(itemDto("Велосипед", "Горный", false), ownerId);

        assertTrue(itemService.searchItem("велосипед").isEmpty());
    }

    @Test
    @DisplayName("searchItem: пустая строка")
    void searchItemEmptyText() {
        itemService.createItem(itemDto("Что-то", "Описание", true), ownerId);

        assertTrue(itemService.searchItem("").isEmpty());
        assertTrue(itemService.searchItem(null).isEmpty());
    }

    @Test
    @DisplayName("searchItem: нет совпадений")
    void searchItemNoMatch() {
        itemService.createItem(itemDto("Стул", "Деревянный стул", true), ownerId);

        assertTrue(itemService.searchItem("ракета").isEmpty());
    }

    private UserCreateRequestDto createUserDto(String email, String name) {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        setField(dto, "email", email);
        setField(dto, "name", name);
        return dto;
    }

    private ItemDto itemDto(String name, String description, boolean available) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private ItemUpdateDto updateDto(String name, String description, Boolean available) {
        ItemUpdateDto dto = new ItemUpdateDto();
        if (name != null) setField(dto, "name", name);
        if (description != null) setField(dto, "description", description);
        if (available != null) setField(dto, "available", available);
        return dto;
    }

    private void setField(Object obj, String field, Object value) {
        try {
            var f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
