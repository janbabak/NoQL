package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
@Import({JwtService.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/chat";

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Create chat")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testCreateChat() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        UUID chatId = UUID.randomUUID();
        ChatDto chatDto = ChatDto.builder()
                .id(chatId)
                .name("New chat")
                .build();
        // language=JSON
        String responseContent =
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

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Create chat in not existing database")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
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
    @DisplayName("Get chat by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
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
        mockMvc.perform(get(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    @DisplayName("Get chat by id not found")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testGetChatByIdNotFound() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        // when
        when(chatService.findById(chatId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Delete chat by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testDeleteChatById() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Rename chat by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testRenameChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Rename chat not found")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testRenameChatNotFound() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        // when
        doThrow(EntityNotFoundException.class).when(chatService).renameChat(chatId, name);

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Delete chat by id too long name")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testDeleteChatByIdTooLongName() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "To long name. More than 32 chars.";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Delete chat by id empty name")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testDeleteChatByIdEmptyName() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Add message to chat")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
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

    @Disabled // TODO: Fix this test
    @Test
    @DisplayName("Add message to chat not found")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testAddMessageToChatNotFound() throws Exception {
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
        when(chatService.addMessageToChat(chatId, request)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}