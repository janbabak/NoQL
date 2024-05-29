package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.database.DatabaseEngine;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatQueryWithResponseRepositoryTest {

    @Autowired
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Autowired
    private DatabaseRepository databaseRepository;

    @AfterEach
    void afterEach() {
        databaseRepository.deleteAll();
    }

    @Test
    void testFindLatestMessage() {
        // given
        Database database = Database.builder()
                .name("Testing database")
                .host("localhost")
                .port(5432)
                .database("/database")
                .userName("user")
                .password("password")
                .engine(DatabaseEngine.POSTGRES)
                .chats(new ArrayList<>())
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
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-26 10:10:10.0"))
                .build();
        ChatQueryWithResponse message2 = ChatQueryWithResponse.builder()
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-27 10:11:10.0"))
                .build();
        ChatQueryWithResponse message3 = ChatQueryWithResponse.builder()
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-27 10:10:10.0"))
                .build();
        ChatQueryWithResponse message4 = ChatQueryWithResponse.builder()
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
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