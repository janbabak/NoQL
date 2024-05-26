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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ChatQueryWithResponseRepositoryTest {

    @Autowired
    private ChatQueryWithResponseRepository chatQueryWithResponseRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private DatabaseRepository databaseRepository;

    @AfterEach
    void afterEach() {
        chatQueryWithResponseRepository.deleteAll();
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

        Chat chat = Chat.builder()
                .name("Testing chat")
                .database(database)
                .modificationDate(Timestamp.valueOf("2024-05-01 10:10:10.0"))
                .messages(new ArrayList<>())
                .build();
        database.addChat(chat);

        ChatQueryWithResponse message1 = ChatQueryWithResponse.builder()
                .chat(chat)
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-26 10:10:10.0"))
                .build();
        ChatQueryWithResponse message2 = ChatQueryWithResponse.builder()
                .chat(chat)
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-27 10:11:10.0"))
                .build();
        ChatQueryWithResponse message3 = ChatQueryWithResponse.builder()
                .chat(chat)
                .nlQuery("SELECT * FROM table")
                .llmResponse("{databaseQuery: \"SELECT * FROM table\"}, pythonCode: null, generatePlot: false")
                .timestamp(Timestamp.valueOf("2024-05-27 10:10:10.0"))
                .build();
        chat
                .addMessage(message1)
                .addMessage(message1)
                .addMessage(message1);

        databaseRepository.save(database);
        chatRepository.save(chat);
        chatQueryWithResponseRepository.save(message1);
        chatQueryWithResponseRepository.save(message2);
        chatQueryWithResponseRepository.save(message3);

        // when
        ChatQueryWithResponse latestMessage = chatQueryWithResponseRepository
                .findLatestMessageFromChat(chat.getId())
                .orElseThrow(() -> new RuntimeException("No message found"));

        // then
        assertEquals(message2.getId(), latestMessage.getId());
    }
}