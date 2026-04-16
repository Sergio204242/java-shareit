package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.shareit.exception.notfound.ItemNotFoundException;
import ru.practicum.shareit.exception.notfound.UserNotFoundException;
import ru.practicum.shareit.item.dal.ItemDbStorage;
import ru.practicum.shareit.item.dto.request.ItemDto;
import ru.practicum.shareit.item.dto.request.ItemUpdateDto;
import ru.practicum.shareit.item.dto.response.ItemResponseDto;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.dal.UserDbStorage;
import ru.practicum.shareit.user.dto.request.UserCreateRequestDto;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemServiceImplTest {

    private ItemServiceImpl itemService;
    private UserServiceImpl userService;

    private long ownerId;

    @BeforeEach
    void setUp() throws Exception {
        UserDbStorage userStorage = new UserDbStorage();
        ItemDbStorage itemStorage = new ItemDbStorage();
        userService = new UserServiceImpl(userStorage);
        itemService = new ItemServiceImpl(itemStorage, userStorage);
        ownerId = userService.create(buildUserDto("owner@example.com", "Owner")).getId();
    }

    @Test
    @DisplayName("createItem: создание item")
    void createItem() {
        ItemDto dto = buildItemDto("Дрель", "Электрическая", true);

        ItemResponseDto result = itemService.createItem(dto, ownerId);

        assertTrue(result.getId() > 0);
        assertEquals("Дрель", result.getName());
        assertEquals("Электрическая", result.getDescription());
        assertTrue(result.isAvailable());
    }

    @Test
    @DisplayName("createItem: несуществующий userId")
    void createItemUnknownUser() {
        ItemDto dto = buildItemDto("Лопата", "Штыковая", true);

        assertThrows(UserNotFoundException.class,
                () -> itemService.createItem(dto, 999L));
    }

    @Test
    @DisplayName("createItem: два разных пользователя создают item")
    void createItemTwoOwners() throws Exception {
        long owner2 = userService.create(buildUserDto("owner2@example.com", "Owner2")).getId();

        itemService.createItem(buildItemDto("Вещь1", "Описание1", true), ownerId);
        itemService.createItem(buildItemDto("Вещь2", "Описание2", true), owner2);

        assertEquals(1, itemService.getItems(ownerId).size());
        assertEquals(1, itemService.getItems(owner2).size());
    }

    @Test
    @DisplayName("updateItem: обновление названия")
    void updateItem() throws Exception {
        ItemResponseDto created = itemService.createItem(
                buildItemDto("Старое название", "Описание", true), ownerId);

        ItemUpdateDto updateDto = buildUpdateDto("Новое название", null, null);
        ItemResponseDto updated = itemService.updateItem(updateDto, created.getId(), ownerId);

        assertEquals("Новое название", updated.getName());
        assertEquals("Описание", updated.getDescription());
    }

    @Test
    @DisplayName("updateItem: обновление описания и доступности")
    void updateItemDescriptionAndAvailable() throws Exception {
        ItemResponseDto created = itemService.createItem(
                buildItemDto("Молоток", "Старое описание", true), ownerId);

        ItemUpdateDto updateDto = buildUpdateDto(null, "Новое описание", false);
        ItemResponseDto updated = itemService.updateItem(updateDto, created.getId(), ownerId);

        assertEquals("Новое описание", updated.getDescription());
        assertFalse(updated.isAvailable());
    }

    @Test
    @DisplayName("updateItem: неверная пара itemId + userId")
    void updateItemWrongOwner() throws Exception {
        ItemResponseDto created = itemService.createItem(
                buildItemDto("Вещь", "Описание", true), ownerId);

        long strangerUserId = userService.create(buildUserDto("stranger@example.com", "Stranger")).getId();
        ItemUpdateDto updateDto = buildUpdateDto("Чужое", null, null);

        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(updateDto, created.getId(), strangerUserId));
    }

    @Test
    @DisplayName("updateItem: несуществующий itemId")
    void updateItemUnknownItem() throws Exception {
        ItemUpdateDto updateDto = buildUpdateDto("X", null, null);

        assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(updateDto, 999L, ownerId));
    }

    @Test
    @DisplayName("getItem: возвращает нужную вещь")
    void getItem() {
        ItemResponseDto created = itemService.createItem(
                buildItemDto("Книга", "Интересная", true), ownerId);

        ItemResponseDto found = itemService.getItem(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Книга", found.getName());
    }

    @Test
    @DisplayName("getItem: несуществующий id")
    void getItemUnknownId() {
        assertThrows(ItemNotFoundException.class,
                () -> itemService.getItem(42L));
    }

    @Test
    @DisplayName("getItems: список вещей пользователя")
    void getItemsReturnsOnlyOwnerItems() throws Exception {
        long owner2 = userService.create(buildUserDto("o2@example.com", "O2")).getId();

        itemService.createItem(buildItemDto("Вещь А", "Описание А", true), ownerId);
        itemService.createItem(buildItemDto("Вещь Б", "Описание Б", true), ownerId);
        itemService.createItem(buildItemDto("Чужая вещь", "Чужое описание", true), owner2);

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
        itemService.createItem(buildItemDto("Электродрель", "Мощная дрель", true), ownerId);
        itemService.createItem(buildItemDto("Перфоратор", "Хороший перфоратор", true), ownerId);

        List<ItemResponseDto> result = itemService.searchItem("дрель");

        assertEquals(1, result.size());
        assertEquals("Электродрель", result.getFirst().getName());
    }

    @Test
    @DisplayName("searchItem: поиск по части описания")
    void searchItemByDescription() {
        itemService.createItem(buildItemDto("Инструмент", "гаечный ключ", true), ownerId);

        List<ItemResponseDto> result = itemService.searchItem("гаечный");

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("searchItem: недоступные вещи не попадают в результат")
    void searchItemUnavailable() {
        itemService.createItem(buildItemDto("Велосипед", "Горный велосипед", false), ownerId);

        List<ItemResponseDto> result = itemService.searchItem("велосипед");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("searchItem: пустая строка")
    void searchItemEmptyText() {
        itemService.createItem(buildItemDto("Что-то", "Описание", true), ownerId);

        assertTrue(itemService.searchItem("").isEmpty());
        assertTrue(itemService.searchItem(null).isEmpty());
    }

    @Test
    @DisplayName("searchItem: нет совпадений")
    void searchItemNoMatch() {
        itemService.createItem(buildItemDto("Стул", "Деревянный стул", true), ownerId);

        assertTrue(itemService.searchItem("ракета").isEmpty());
    }

    private UserCreateRequestDto buildUserDto(String email, String name) throws Exception {
        UserCreateRequestDto dto = new UserCreateRequestDto();
        setField(dto, "email", email);
        setField(dto, "name", name);
        return dto;
    }

    private ItemDto buildItemDto(String name, String description, boolean available) {
        ItemDto dto = new ItemDto();
        dto.setName(name);
        dto.setDescription(description);
        dto.setAvailable(available);
        return dto;
    }

    private ItemUpdateDto buildUpdateDto(String name, String description, Boolean available) throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto();
        if (name != null) setField(dto, "name", name);
        if (description != null) setField(dto, "description", description);
        if (available != null) setField(dto, "available", available);
        return dto;
    }

    private void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
