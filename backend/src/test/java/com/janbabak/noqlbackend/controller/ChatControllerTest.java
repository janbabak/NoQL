package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/chat";

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

}