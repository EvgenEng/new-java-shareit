package ru.practicum.shareit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ItemControllerIntegrationTest {
        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        private long userId;

        @BeforeEach
        void setUp() throws Exception {
                UserDto userToCreate = UserDto.builder().name("Owner").email("owner@example.com").build();
                String response = mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userToCreate)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                UserDto createdUser = objectMapper.readValue(response, UserDto.class);
                this.userId = createdUser.getId();
        }

        @Test
        void shouldCreateItemAndThenSearchForIt() throws Exception {
                ItemDto itemToCreate = ItemDto.builder().name("Power Drill").description("Very powerful drill")
                                .available(true).build();

                // Create
                mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemToCreate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1))
                                .andExpect(jsonPath("$.name").value("Power Drill"));

                // Search
                mockMvc.perform(get("/items/search").param("text", "drill"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Power Drill"));
        }

        @Test
        void shouldUpdateItem() throws Exception {
                ItemDto itemToCreate = ItemDto.builder().name("Screwdriver").description("Manual screwdriver")
                                .available(true).build();

                String response = mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemToCreate)))
                                .andExpect(status().isOk())
                                .andReturn().getResponse().getContentAsString();

                ItemDto createdItem = objectMapper.readValue(response, ItemDto.class);

                ItemDto itemToUpdate = ItemDto.builder().name("Electric Screwdriver").description("Battery powered")
                                .available(true).build();

                // Update
                mockMvc.perform(patch("/items/" + createdItem.getId())
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemToUpdate)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Electric Screwdriver"));
        }
}
