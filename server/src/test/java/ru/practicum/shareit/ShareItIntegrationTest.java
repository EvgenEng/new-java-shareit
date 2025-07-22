package ru.practicum.shareit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ShareItIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldSearchItems() throws Exception {
                // Создаем пользователя
                UserDto owner = UserDto.builder().name("Owner").email("owner@example.com").build();
                String ownerResponse = mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(owner)))
                                .andExpect(status().isCreated())
                                .andReturn().getResponse().getContentAsString();

                UserDto createdOwner = objectMapper.readValue(ownerResponse, UserDto.class);

                ItemDto drill = ItemDto.builder().name("Drill").description("Powerful electric drill").available(true)
                                .build();
                mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", createdOwner.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(drill)))
                                .andExpect(status().isOk());

                ItemDto hammer = ItemDto.builder().name("Hammer").description("Heavy construction hammer")
                                .available(true).build();
                mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", createdOwner.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(hammer)))
                                .andExpect(status().isOk());

                mockMvc.perform(get("/items/search")
                                .param("text", "drill"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Drill"));

                mockMvc.perform(get("/items/search")
                                .param("text", "construction"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Hammer"));
        }
}
