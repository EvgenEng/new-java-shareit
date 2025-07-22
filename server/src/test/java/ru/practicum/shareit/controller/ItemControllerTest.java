package ru.practicum.shareit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingDto;
import ru.practicum.shareit.item.servece.ItemService;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ItemService itemService;

        private ItemDto itemDto;
        private ItemWithBookingDto itemWithBookingDto;
        private CommentDto commentDto;
        private final long userId = 1L;

        @BeforeEach
        void setUp() {
                itemDto = new ItemDto(1L, "Drill", "Powerful drill", true, null);

                itemWithBookingDto = new ItemWithBookingDto();
                itemWithBookingDto.setId(1L);
                itemWithBookingDto.setName("Drill");
                itemWithBookingDto.setDescription("Powerful drill");
                itemWithBookingDto.setAvailable(true);
                itemWithBookingDto.setComments(List.of());

                commentDto = new CommentDto(1L, "Great item!", "John Doe", LocalDateTime.now());
        }

        @Test
        void shouldCreateItem() throws Exception {
                when(itemService.createItem(anyLong(), any(ItemDto.class))).thenReturn(itemDto);

                mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.name").value("Drill"));
        }

        @Test
        void shouldNotCreateItemIfUserNotFound() throws Exception {
                when(itemService.createItem(anyLong(), any(ItemDto.class)))
                                .thenThrow(new NotFoundException("User not found"));

                mockMvc.perform(post("/items")
                                .header("X-Sharer-User-Id", 99L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(itemDto)))
                                .andExpect(status().isNotFound());
        }

        @Test
        void shouldUpdateItem() throws Exception {
                ItemDto updatedDto = new ItemDto(1L, "Updated Drill", "More powerful", false, null);
                when(itemService.updateItem(anyLong(), anyLong(), any(ItemDto.class))).thenReturn(updatedDto);

                mockMvc.perform(patch("/items/1")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updatedDto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("Updated Drill"))
                                .andExpect(jsonPath("$.available").value(false));
        }

        @Test
        void shouldGetItemById() throws Exception {
                when(itemService.getItemById(anyLong(), anyLong())).thenReturn(itemWithBookingDto);

                mockMvc.perform(get("/items/1")
                                .header("X-Sharer-User-Id", userId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.name").value("Drill"));
        }

        @Test
        void shouldGetItemsByOwner() throws Exception {
                when(itemService.getItemsByOwner(anyLong(), anyInt(), anyInt()))
                                .thenReturn(List.of(itemWithBookingDto));

                mockMvc.perform(get("/items")
                                .header("X-Sharer-User-Id", userId)
                                .param("from", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].id").value(1L));
        }

        @Test
        void shouldSearchItems() throws Exception {
                when(itemService.searchItems(anyString(), anyInt(), anyInt())).thenReturn(List.of(itemDto));

                mockMvc.perform(get("/items/search")
                                .param("text", "drill")
                                .param("from", "0")
                                .param("size", "10"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].name").value("Drill"));
        }

        @Test
        void shouldAddComment() throws Exception {
                CommentDto newComment = new CommentDto(null, "Great item!", null, null);
                when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                                .thenReturn(commentDto);

                mockMvc.perform(post("/items/1/comment")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newComment)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.text").value("Great item!"))
                                .andExpect(jsonPath("$.authorName").value("John Doe"));
        }

        @Test
        void shouldNotAddCommentIfUserDidNotBookItem() throws Exception {
                CommentDto newComment = new CommentDto(null, "Great item!", null, null);
                when(itemService.addComment(anyLong(), anyLong(), any(CommentDto.class)))
                                .thenThrow(new ValidationException("User must have finished booking to leave comment"));

                mockMvc.perform(post("/items/1/comment")
                                .header("X-Sharer-User-Id", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newComment)))
                                .andExpect(status().isBadRequest());
        }
}
