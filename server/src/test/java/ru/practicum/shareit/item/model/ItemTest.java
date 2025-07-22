package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void testItemConstructor() {
        // given
        String name = "Test Item";
        String description = "Test Description";
        Boolean available = true;

        // when
        Item item = new Item(name, description, available);

        // then
        assertEquals(name, item.getName());
        assertEquals(description, item.getDescription());
        assertEquals(available, item.getAvailable());
    }
}