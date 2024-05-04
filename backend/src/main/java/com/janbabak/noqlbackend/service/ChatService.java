package com.janbabak.noqlbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.chat.LLMResponse;
import com.janbabak.noqlbackend.model.chat.CreateMessageWithResponseRequest;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponseDto;
import com.janbabak.noqlbackend.service.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;
import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.DATABASE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatQueryWithResponseRepository messageRepository;
    private final DatabaseRepository databaseRepository;
    private final String NEW_CHAT_NAME = "New chat";
    private final int CHAT_NAME_MAX_LENGTH = 32;

    /**
     * Find chat by chat id.
     *
     * @param id identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found
     */
    public ChatDto findById(UUID id) throws EntityNotFoundException {
        log.info("Get chat by id={}", id);

        Chat chat = chatRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CHAT, id));

        return new ChatDto(
                chat.getId(),
                chat.getName(),
                chat.getMessages()
                        .stream()
                        .map(message -> {
                            try {
                                LLMResponse LLMResponse = JsonUtils.createLLMResponse(message.getResponse());

                                return new ChatQueryWithResponseDto(
                                        message.getId(),
                                        message.getMessage(),
                                        new ChatQueryWithResponseDto.ChatResponseResult(
                                                LLMResponse.getDatabaseQuery(),
                                                "/static/images/plot.png"),
                                        message.getTimestamp());
                            } catch (JsonProcessingException e) {
                                throw new RuntimeException(e); // TODO resolve
                            }
                        })
                        .toList(),
                chat.getModificationDate());
    }


    /**
     * Find all chats associated with specified database sorted by the modification date in descending order.
     *
     * @param databaseId database identifier
     * @return list of chats
     * @throws EntityNotFoundException database of specified identifier not found.
     */
    public List<ChatHistoryItem> findChatsByDatabaseId(UUID databaseId) throws EntityNotFoundException {
        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        return chatRepository.findAllByDatabaseOrderByModificationDateDesc(database)
                .stream()
                .map((chat -> ChatHistoryItem
                        .builder()
                        .id(chat.getId())
                        .name(chat.getName())
                        .build()))
                .toList();
    }

    /**
     * Create new chat object - persis it.
     *
     * @param databaseId identifier of the associated db
     * @return saved object with id
     */
    public ChatDto create(UUID databaseId) throws EntityNotFoundException {
        log.info("Create new chat.");

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        Chat chat = Chat.builder()
                .name(NEW_CHAT_NAME)
                .modificationDate(Timestamp.from(Instant.now()))
                .database(database)
                .build();

        chat = chatRepository.save(chat);

        return new ChatDto(chat.getId(), chat.getName(), List.of(), chat.getModificationDate());
    }

    /**
     * Add message to chat
     *
     * @param chatId  chat identifier
     * @param request message with response
     * @return created message with response
     * @throws EntityNotFoundException chat of specified id not found.
     */
    public ChatQueryWithResponse addMessageToChat(UUID chatId, CreateMessageWithResponseRequest request)
            throws EntityNotFoundException {

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        Timestamp timestamp = Timestamp.from(Instant.now());
        ChatQueryWithResponse message = ChatQueryWithResponse.builder()
                .chat(chat)
                .message(request.getMessage())
                .response(request.getResponse())
                .timestamp(timestamp)
                .build();

        chat.addMessage(message);
        chat.setModificationDate(timestamp);

        if (Objects.equals(chat.getName(), NEW_CHAT_NAME)) {
            chat.setName(message.getMessage().length() < CHAT_NAME_MAX_LENGTH
                    ? message.getMessage() : message.getMessage().substring(0, CHAT_NAME_MAX_LENGTH));
        }
        chatRepository.save(chat);
        return messageRepository.save(message);
    }

    /**
     * Rename chat. If the new name is longer than {@code CHAT_NAME_MAX_LENGTH},
     * use only the first {@code CHAT_NAME_MAX_LENGTH} characters
     *
     * @param id   chat identifier
     * @param name new name
     * @throws EntityNotFoundException chat of specified id not found.
     */
    public void renameChat(UUID id, String name) throws EntityNotFoundException {
        Chat chat = chatRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(CHAT, id));
        chat.setName(name.length() < CHAT_NAME_MAX_LENGTH ? name : name.substring(0, CHAT_NAME_MAX_LENGTH));
        chatRepository.save(chat);
    }

    /**
     * Delete chat by id.
     *
     * @param id chat identifier
     */
    public void deleteChatById(UUID id) {
        chatRepository.deleteById(id);
    }
}
