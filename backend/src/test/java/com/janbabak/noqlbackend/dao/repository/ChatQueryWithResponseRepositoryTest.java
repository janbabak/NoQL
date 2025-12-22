package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatQueryWithResponseRepositoryTest {

    @Autowired
    @SuppressWarnings("unused")
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Autowired
    @SuppressWarnings("unused")
    private DatabaseRepository databaseRepository;

    @Autowired
    @SuppressWarnings("unused")
    private UserRepository userRepository;

    @AfterEach
    void afterEach() {
        databaseRepository.deleteAll();
    }

    @Test
    void testFindLatestMessage() {
        // given
        User user = User.builder()
                .id(UUID.randomUUID())
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@hotmail.com")
                .password("password")
                .build();

        userRepository.save(user);


        Database database = Database.builder()
                .name("Testing database")
                .host("localhost")
                .port(5432)
                .database("/database")
                .userName("user")
                .password("password")
                .engine(DatabaseEngine.POSTGRES)
                .chats(new ArrayList<>())
                .user(user)
                .build();

        Chat chat1 = Chat.builder()
                .name("Testing chat")
                .modificationDate(Timestamp.valueOf("2024-05-01 10:10:10.0"))
                .messages(new ArrayList<>())
                .build();
        Chat chat2 = Chat.builder()
                .name("New chat")
                .modificationDate(Timestamp.valueOf("2024-05-01 10:10:10.0"))
                .messages(new ArrayList<>())
                .build();
        database.addChats(List.of(chat1, chat2));

        ChatQueryWithResponse message1 = ChatQueryWithResponse.builder()
                .chat(chat1)
                .nlQuery("Show data from table")
                .dbQuery("SELECT * FROM table")
                .dbQueryExecutionSuccess(true)
                .timestamp(Timestamp.valueOf("2024-05-26 10:10:10.0"))
                .build();
        ChatQueryWithResponse message2 = ChatQueryWithResponse.builder()
                .nlQuery("Get all records from table")
                .dbQuery("SELECT all FROM table")
                .dbQueryExecutionSuccess(false)
                .dbExecutionErrorMessage("Error executing query")
                .timestamp(Timestamp.valueOf("2024-05-27 10:11:10.0"))
                .build();
        ChatQueryWithResponse message3 = ChatQueryWithResponse.builder()
                .nlQuery("plot users by age")
                .plotScript("import matplotlib.pyplot as plt; ...")
                .plotGenerationSuccess(false)
                .plotGenerationErrorMessage("Error generating plot")
                .timestamp(Timestamp.valueOf("2024-05-27 10:10:10.0"))
                .build();
        ChatQueryWithResponse message4 = ChatQueryWithResponse.builder()
                .nlQuery("Find all entries")
                .dbQuery("SELECT * FROM table")
                .dbQueryExecutionSuccess(true)
                .plotScript("import matplotlib.pyplot as plt; ...")
                .plotGenerationSuccess(true)
                .timestamp(Timestamp.valueOf("2024-05-28 10:10:10.0"))
                .build();
        chat1.addMessages(List.of(message1, message2, message3));
        chat2.addMessage(message4);

        databaseRepository.save(database);

        // when
        ChatQueryWithResponse latestMessage = chatQueryWithResponseRepository
                .findLatestMessageFromChat(chat1.getId())
                .orElseThrow(() -> new RuntimeException("No message found"));

        // then
        assertEquals(message2.getId(), latestMessage.getId());
    }
}