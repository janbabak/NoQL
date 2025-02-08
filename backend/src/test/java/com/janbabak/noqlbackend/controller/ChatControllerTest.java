package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
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
    @DisplayName("Create chat - success")
    @WithMockUser(roles = "USER")
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
    @DisplayName("Create chat - database not found")
    @WithMockUser(roles = "USER")
    void testCreateChatDatabaseNotFound() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

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
    @DisplayName("Create chat - user not owner of database")
    @WithMockUser(roles = "USER")
    void testCreateChatUserNotOwnerOfDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        when(chatServiceMock.create(databaseId))
                .thenThrow(new AccessDeniedException("Access denied"));

        // then
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", databaseId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Create chat - anonymous user")
    @WithAnonymousUser
    void testCreateChatAnonymousUser() throws Exception {
        mockMvc.perform(post(ROOT_URL)
                        .param("databaseId", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get chat by id - success")
    @WithMockUser(roles = "USER")
    void testGetChatById() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        ChatDto chatDto = ChatDto.builder()
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
    @DisplayName("Get chat by id - not found")
    @WithMockUser(roles = "USER")
    void testGetChatByIdNotFound() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatServiceMock.findById(eq(chatId), any(), eq(true))).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Get chat by id - anonymous user")
    @WithAnonymousUser
    void testGetChatByIdAnonymousUser() throws Exception {
        mockMvc.perform(get(ROOT_URL + "/{chatId}", UUID.randomUUID()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Get chat by id - user not owner of chat")
    @WithMockUser(roles = "USER")
    void testGetChatByIdUserNotOwnerOfChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        when(chatServiceMock.findById(eq(chatId), any(), eq(true)))
                .thenThrow(new AccessDeniedException("Access denied"));

        // then
        mockMvc.perform(get(ROOT_URL + "/{chatId}", chatId))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Delete chat by id - success")
    @WithMockUser(roles = "USER")
    void testDeleteChatById() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        // then
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", chatId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete chat - anonymous user")
    @WithAnonymousUser
    void testDeleteChatByIdAnonymousUser() throws Exception {
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", UUID.randomUUID())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Delete chat by id - usen not owner of chat")
    @WithMockUser(roles = "USER")
    void testDeleteChatByIdUserNotOwnerOfChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();

        doThrow(new AccessDeniedException("Access denied"))
                .when(chatServiceMock).deleteChatById(chatId);

        // then
        mockMvc.perform(delete(ROOT_URL + "/{chatId}", chatId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Rename chat by id - success")
    @WithMockUser(roles = "USER")
    void testRenameChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Rename chat - not found")
    @WithMockUser(roles = "USER")
    void testRenameChatNotFound() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        doThrow(EntityNotFoundException.class).when(chatServiceMock).renameChat(chatId, name);

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Rename chat - name too long")
    @WithMockUser(roles = "USER")
    void testRenameChatNameTooLong() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "To long name. More than 32 chars.";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rename chat - empty name")
    @WithMockUser(roles = "USER")
    void testRenameChatEmtpyName() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "";

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Rename chat - anonymous user")
    @WithAnonymousUser
    void testRenameChatAnonymousUser() throws Exception {
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", UUID.randomUUID())
                        .param("name", "Find all users")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Rename chat - user not owner of chat")
    @WithMockUser(roles = "USER")
    void testRenameChatUserNotOwnerOfChat() throws Exception {
        // given
        UUID chatId = UUID.randomUUID();
        String name = "Find all users";

        doThrow(new AccessDeniedException("Access denied"))
                .when(chatServiceMock).renameChat(chatId, name);

        // then
        mockMvc.perform(put(ROOT_URL + "/{chatId}/name", chatId)
                        .param("name", name)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Add message to chat - success")
    @WithMockUser(roles = "USER")
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

        when(chatServiceMock.addMessageToChat(chatId, request)).thenReturn(null);


        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Add message to chat - chat not found")
    @WithMockUser(roles = "USER")
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

        when(chatServiceMock.addMessageToChat(chatId, request)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Add message to chat - anonymous user")
    @WithAnonymousUser
    void testAddMessageToChaAnonymousUser() throws Exception {
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(CreateChatQueryWithResponseRequest.builder().build()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Add message to chat - user not owner of chat")
    @WithMockUser(roles = "USER")
    void testAddMessageToChatUserNotOwnerOfChat() throws Exception {
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

        when(chatServiceMock.addMessageToChat(chatId, request))
                .thenThrow(new AccessDeniedException("Access denied"));

        // then
        mockMvc.perform(post(ROOT_URL + "/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}