package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.query.*;
import com.janbabak.noqlbackend.service.JwtService;
import com.janbabak.noqlbackend.service.QueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import(JwtService.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QueryService queryService;

    private final String ROOT_URL = "/message";

    @Test
    @DisplayName("Load chat result")
    @WithMockUser(roles = "USER")
    void testLoadChatResult() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        Integer page = 1;
        Integer pageSize = 2;
        ChatResponseData response = ChatResponseData.builder()
                .page(page)
                .pageSize(pageSize)
                .totalCount(10L)
                .columnNames(List.of("name", "email", "age"))
                .rows(List.of(
                        List.of("John", "john@gmail.com", "26"),
                        List.of("Lenny", "lenny@gmail.com", "65")))
                .build();

        when(queryService.loadChatResponseData(messageId, page, pageSize)).thenReturn(response);

        // then
        mockMvc.perform(
                        get(ROOT_URL + "/{messageId}/data",
                                messageId, page, pageSize)
                                .param("page", page.toString())
                                .param("pageSize", pageSize.toString())
                                .param("messageId", messageId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(response), true));
    }

    @Test
    @DisplayName("Load chat result by anonymous user")
    @WithAnonymousUser
    void testLoadChatResultByAnotherUser() throws Exception {
        // given
        UUID messageId = UUID.randomUUID();
        Integer page = 1;
        Integer pageSize = 2;

        // then
        mockMvc.perform(
                        get(ROOT_URL + "/{messageId}/data", messageId, page, pageSize)
                                .param("page", page.toString())
                                .param("pageSize", pageSize.toString())
                                .param("messageId", messageId.toString()))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

}