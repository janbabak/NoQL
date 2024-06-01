package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatabaseController.class)
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseEntityService databaseEntityService;

    @MockBean
    private QueryService queryService;

    @MockBean
    private ChatService chatService;

    private final String ROOT_URL = "/database";

    @Test
    void testGetDatabaseById() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();
        Database database = Database.builder()
                .id(databaseId)
                .name("Local Postgres")
                .host("localhost")
                .port(5432)
                .database("database")
                .userName("user")
                .password("password")
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.POSTGRES)
                .build();

        // when
        when(databaseEntityService.findById(databaseId)).thenReturn(database);

        // then
        mockMvc
                .perform(get(ROOT_URL + "/{id}", databaseId))
                .andDo(print())
                .andExpect(content().json("""
                        {
                            "id":"%s",
                            "name":"Local Postgres",
                            "host":"localhost",
                            "port":5432,
                            "database":"database",
                            "userName":"user",
                            "password":"password",
                            "engine":"POSTGRES"
                        }
                        """.formatted(databaseId))
                )
                .andExpect(status().isOk());
    }

    @Test
    void testGetNotExistingDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseEntityService.findById(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{id}", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}