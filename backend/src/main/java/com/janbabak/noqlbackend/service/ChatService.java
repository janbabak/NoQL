package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.MessageWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.CreateMessageWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.MessageWithResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;
import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MessageWithResponseRepository messageRepository;
    private final DatabaseRepository databaseRepository;

    /**
     * Find chat by chat id
     *
     * @param id identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found
     */
    public Chat findById(UUID id) throws EntityNotFoundException {
        log.info("Get chat by id={}", id);

        return chatRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CHAT, id));
    }

    /**
     * Create new chat object - persis it.
     * @param databaseId identifier of the associated db
     * @return saved object with id
     */
    public Chat create(UUID databaseId) throws EntityNotFoundException {
        log.info("Create new chat.");

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        Chat chat = Chat.builder()
                .name("New chat")
                .modificationDate(Timestamp.from(Instant.now()))
                .database(database)
                .build();

        return chatRepository.save(chat);
    }

    /**
     * Add message to chat
     * @param chatId chat identifier
     * @param request message with response
     * @throws EntityNotFoundException chat of specified id not found.
     */
    public void addMessageToChat(UUID chatId, CreateMessageWithResponseRequest request) throws EntityNotFoundException {
        Chat chat = findById(chatId);

        MessageWithResponse message = MessageWithResponse.builder()
                .chat(chat)
                .message(request.getMessage())
                .response(request.getResponse())
                .timestamp(Timestamp.from(Instant.now()))
                .build();

        chat.addMessage(message);
        messageRepository.save(message);
    }

    /**
     * Rename chat.
     * @param id chat identifier
     * @param name new name
     * @throws EntityNotFoundException chat of specified id not found.
     */
    public void renameChat(UUID id, String name) throws EntityNotFoundException {
        Chat chat = findById(id);
        chat.setName(name);
        chatRepository.save(chat);
    }

    /**
     * Delete chat by id.
     * @param id chat identifier
     */
    public void deleteChatById(UUID id) {
        chatRepository.deleteById(id);
    }
}
