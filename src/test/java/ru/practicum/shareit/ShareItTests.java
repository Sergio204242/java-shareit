package ru.practicum.shareit;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.service.ItemService;

@SpringBootTest
@RequiredArgsConstructor
class ShareItTests {

    @Autowired
    private ItemService itemService;

    @Test
    void contextLoads() {
    }
}
