package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
import static org.mockito.Mockito.doThrow;
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
        String responseContent =
                // language=JSON
                 """
                 {
                     "id":"%s",
                     "name":"New chat",
                     "messages":null,
                     "modificationDate":null
                }
                """.formatted(chatId);

        // when
        when(chatService.create(databaseId)).thenReturn(chatDto);

        // then
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(responseContent));
    }

    @Test
    void testCreateChatNotExistingDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(chatService.create(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetChatById() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        ChatDto chatDto = ChatDto.builder()
                .id(chatId)
                .name("Test chat")
                .build();

        // when
        when(chatService.findById(chatId)).thenReturn(chatDto);

        // then
        mockMvc.perform(get(ROOT_URL + "/{id}", chatId))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    void testGetNotExistingChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        // when
        when(chatService.findById(chatId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{id}", chatId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteChatById() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{id}", chatId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testRenameChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        // then
        mockMvc.perform(put(ROOT_URL + "/{id}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    void testRenameNotExistingChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        // when
        doThrow(EntityNotFoundException.class).when(chatService).renameChat(chatId, name);

        // then
        mockMvc.perform(put(ROOT_URL + "/{id}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddMessageToChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        CreateChatQueryWithResponseRequest request = CreateChatQueryWithResponseRequest.builder()
                .nlQuery("Find all users older than 25")
                .llmResult(
                        // language=JSON
                        """
                        {
                             "databaseQuery": "SELECT * FROM users WHERE age > 25",
                             "generatePlot": false,
                             "pythonCode": null
                         }""")
                .build();

        // when
        when(chatService.addMessageToChat(chatId, request)).thenReturn(null);


        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testAddMessageToNotExistingChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        CreateChatQueryWithResponseRequest request = CreateChatQueryWithResponseRequest.builder()
                .nlQuery("Find all users older than 25")
                .llmResult(// language=JSON
                        """
                        {
                             "databaseQuery": "SELECT * FROM users WHERE age > 25",
                             "generatePlot": false,
                             "pythonCode": null
                         }""")
                .build();

        // when
        when(chatService.addMessageToChat(chatId, request)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}