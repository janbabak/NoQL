package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.chat.ChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({JwtService.class})
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatServiceMock;

    private final String ROOT_URL = "/chat";

    @Test
    @DisplayName("Create chat")
    @WithMockUser(roles = "USER")
    void testCreateChat() throws Exception {
        // given
        final UUID databaseId = UUID.randomUUID();
        final UUID chatId = UUID.randomUUID();
        final ChatDto chatDto = ChatDto.builder()
                .id(chatId)
                .name("New chat")
                .build();
        // language=JSON
        final String responseContent =
                """
                         {
                             "id":"%s",
                             "name":"New chat",
                             "messages":null,
                             "modificationDate":null
                        }
                        """.formatted(chatId);

        when(chatServiceMock.create(databaseId)).thenReturn(chatDto);

        // then
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(responseContent));
    }

    @Test
    @DisplayName("Create chat in not existing database")
    @WithMockUser(roles = "USER")
    void testCreateChatNotExistingDatabase() throws Exception {
        // given
        final UUID databaseId = UUID.randomUUID();

        when(chatServiceMock.create(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create chat with anonymous user")
    @WithAnonymousUser
    void testCreateChatWithAnonymousUser() throws Exception {
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get chat by id")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testGetChatById() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();
        final ChatDto chatDto = ChatDto.builder()
                .id(chatId)
                .name("Test chat")
                .build();

        when(chatServiceMock.findById(eq(chatId), any(), eq(true))).thenReturn(chatDto);

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
        final UUID chatId = UUID.randomUUID();

        when(chatServiceMock.findById(eq(chatId), any(), eq(true))).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get chat by id with anonymous user")
    @WithAnonymousUser
    void testGetChatByIdWithAnonymousUser() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/{chatId}", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Delete chat by id")
    @WithMockUser(roles = "USER")
    void testDeleteChatById() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", chatId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete chat with anonymous user")
    @WithAnonymousUser
    void testDeleteChatByIdWithAnonymousUser() throws Exception {
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", UUID.randomUUID())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rename chat by id")
    @WithMockUser(roles = "USER")
    void testRenameChat() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();
        final String name = "Find all users";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Rename chat not found")
    @WithMockUser(roles = "USER")
    void testRenameChatNotFound() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();
        final String name = "Find all users";

        doThrow(EntityNotFoundException.class).when(chatServiceMock).renameChat(chatId, name);

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Rename chat by id with too long name")
    @WithMockUser(roles = "USER")
    void testRenameChatNameTooLong() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();
        final String name = "To long name. More than 32 chars.";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rename chat by id with empty name")
    @WithMockUser(username = "john.doe@gmail.com", roles = "USER")
    void testRenameChatEmtpyName() throws Exception {
        // given
        final UUID chatId = UUID.randomUUID();
        final String name = "";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rename chat with anonymous user")
    @WithAnonymousUser
    void testRenameChatWithAnonymousUser() throws Exception {
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", UUID.randomUUID())
                        .param("name", "Find all users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}