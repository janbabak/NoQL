package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.service.utils.JsonUtils.createDatabase;
import static com.janbabak.noqlbackend.service.utils.JsonUtils.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DatabaseController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DatabaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DatabaseEntityService databaseService;

    @MockBean
    @SuppressWarnings("unused") // mock is used internally
    private QueryService queryService;

    @MockBean
    @SuppressWarnings("unused") // mock is used internally
    private ChatService chatService;

    private final String ROOT_URL = "/database";

    private final Database localPostgres = Database.builder()
            .id(UUID.randomUUID())
            .name("Local Postgres")
            .host("localhost")
            .port(5432)
            .database("database")
            .userName("user")
            .password("password")
            .chats(new ArrayList<>())
            .engine(DatabaseEngine.POSTGRES)
            .build();

    @Test
    @DisplayName("Get all databases")
    void testGetAllDatabases() throws Exception {
        // given
        Database localMysql = Database.builder()
                .id(UUID.randomUUID())
                .name("Local MySQL")
                .host("localhost")
                .port(3306)
                .database("database")
                .userName("user")
                .password("password")
                .chats(new ArrayList<>())
                .engine(DatabaseEngine.MYSQL)
                .build();
        List<Database> databases = List.of(localPostgres, localMysql);

        // when
        when(databaseService.findAll()).thenReturn(databases);

        // then
        mockMvc.perform(get(ROOT_URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(toJson(databases), true));
    }

    @Test
    @DisplayName("Get database by id")
    void testGetDatabaseById() throws Exception {
        // when
        when(databaseService.findById(localPostgres.getId())).thenReturn(localPostgres);

        // then
        mockMvc.perform(get(ROOT_URL + "/{id}", localPostgres.getId()))
                .andDo(print())
                .andExpect(content().json(toJson(localPostgres), true))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Get not existing database")
    void testGetNotExistingDatabase() throws Exception {
        // given
        UUID databaseId = UUID.randomUUID();

        // when
        when(databaseService.findById(databaseId)).thenThrow(EntityNotFoundException.class);

        // then
        mockMvc.perform(get(ROOT_URL + "/{id}", databaseId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @DisplayName("Create database")
    @MethodSource("createDatabaseDataProvider")
    void testCreateDatabase(String request, String response, Boolean success) throws Exception {
        // when
        if (success) {
            when(databaseService.create(any())).thenReturn(createDatabase(response));
        }

        // then
        mockMvc.perform(post(ROOT_URL)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isCreated() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, response, and success
     */
    Object[][] createDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":-100,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                        "port": "must be greater than or equal to 1"
                    }""",
                        false,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres database for testing purposes",
                        "host":"localhost",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user",
                        "password":"password password password password password password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 32"
                     }""",
                        false
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"",
                        "host":"",
                        "port":100,
                        "database":"",
                        "userName":"",
                        "password":"",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                          "name": "must not be blank",
                          "host": "must not be blank",
                          "database": "must not be blank",
                          "userName": "must not be blank",
                          "password": "must not be blank"
                      }""",
                        false
                }
        };
    }

    @ParameterizedTest
    @DisplayName("Update database")
    @MethodSource("updateDatabaseDataProvider")
    void testUpdateDatabase(String request, String response, Boolean success) throws Exception {
        // given
        UUID databaseId = UUID.fromString("6678fc72-1a55-4146-b74b-b3f5aac677df");

        // when
        if (success) {
            when(databaseService.update(eq(databaseId), any())).thenReturn(createDatabase(response));
        }

        // then
        mockMvc.perform(put(ROOT_URL + "/{id}", databaseId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andDo(print())
                .andExpect(success ? status().isOk() : status().isBadRequest())
                .andExpect(content().json(response, true));
    }

    /**
     * @return request, response, and success flag
     */
    Object[][] updateDatabaseDataProvider() {
        return new Object[][]{
                {
                        // language=JSON
                        """
                    {
                        "name":"Updated name"
                    }""",
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Updated name",
                        "host":"localhost",
                        "port":5432,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Updated name",
                        "host":"127.0.0.1",
                        "port":5555,
                        "database":"Updated database",
                        "userName":"Updated user",
                        "password":"Updated password",
                        "engine":"MYSQL"
                    }""",
                        // language=JSON
                        """
                    {
                        "id": "6678fc72-1a55-4146-b74b-b3f5aac677df",
                        "name":"Updated name",
                        "host":"127.0.0.1",
                        "port":5555,
                        "database":"Updated database",
                        "userName":"Updated user",
                        "password":"Updated password",
                        "engine":"MYSQL"
                    }""",
                        true,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres",
                        "host":"localhost",
                        "port":-100,
                        "database":"database",
                        "userName":"user",
                        "password":"password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                        "port": "must be greater than or equal to 1"
                    }""",
                        false,
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"Local Postgres database for testing purposes",
                        "host":"localhost",
                        "port":5432,
                        "database": "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "userName":"user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user user",
                        "password":"password password password password password password",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                         "name": "length must be between 1 and 32",
                         "database":"length must be between 1 and 253",
                         "userName":"length must be between 1 and 128",
                         "password":"length must be between 1 and 32"
                     }""",
                        false
                },
                {
                        // language=JSON
                        """
                    {
                        "name":"",
                        "host":"",
                        "port":100,
                        "database":"",
                        "userName":"",
                        "password":"",
                        "engine":"POSTGRES"
                    }""",
                        // language=JSON
                        """
                    {
                        "name": "length must be between 1 and 32",
                        "host": "length must be between 1 and 253",
                        "database": "length must be between 1 and 253",
                        "userName": "length must be between 1 and 128",
                        "password": "length must be between 1 and 32"
                    }""",
                        false
                }
        };
    }
}