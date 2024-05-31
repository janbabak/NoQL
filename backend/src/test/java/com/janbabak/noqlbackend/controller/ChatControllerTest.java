package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/chat";

    @Test
    void testCreateChat() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        ChatDto chatDto = ChatDto.builder()
                .id(chatId)
                .name("New chat")
                .build();
        String responseContent = """
                 {
                     "id":"%s",
                     "name":"New chat",
                     "messages":null,
                     "modificationDate":null,
                }
                """.formatted(chatId);

        // when
        when(chatService.create(databaseId)).thenReturn(chatDto);

        // then
        mockMvc
                .perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(responseContent));
    }

    @Test
    void testGetChatById() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        ChatDto chatDto = ChatDto.builder()
                .id(id)
                .name("Test chat")
                .build();

        // when
        when(chatService.findById(id)).thenReturn(chatDto);

        // then
        mockMvc
                .perform(get(ROOT_URL + "/{id}", id))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void testGetChatByIdNotFound() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        // when
        when(chatService.findById(id)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc
                .perform(get(ROOT_URL + "/{id}", id))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteChatById() throws Exception {
        // given
        UUID id = UUID.randomUUID();

        // then
        mockMvc
                .perform(delete(ROOT_URL + "/{id}", id))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testRenameChat() throws Exception {
        // given
        UUID id = UUID.randomUUID();
        String name = "Find all users";

        // then
        mockMvc
                .perform(put(ROOT_URL + "/{id}/name", id)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

}