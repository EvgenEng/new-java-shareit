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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Test
        void shouldCreateAndThenGetUser() throws Exception {
                UserDto userToCreate = UserDto.builder().name("Test User").email("test@example.com").build();

                // Create user
                MvcResult result = mockMvc.perform(post("/users")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userToCreate)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Test User"))
                                .andReturn();

                UserDto createdUser = objectMapper.readValue(result.getResponse().getContentAsString(), UserDto.class);

                mockMvc.perform(get("/users/" + createdUser.getId()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(createdUser.getId()))
                                .andExpect(jsonPath("$.email").value("test@example.com"));
        }
}
