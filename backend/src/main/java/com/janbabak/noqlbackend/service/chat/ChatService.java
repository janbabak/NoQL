package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.DatabaseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.*;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.ChatResponse;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import com.janbabak.noqlbackend.service.PlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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
    private final PlotService plotService;
    private final AuthenticationService authenticationService;
    private final MessageDataDAO messageDataDAO;
    public static final String NEW_CHAT_NAME = "New chat";

    @SuppressWarnings("FieldCanBeLocal")
    private final int CHAT_NAME_MAX_LENGTH = 32;

    /**
     * Find chat by chat id.
     *
     * @param chatId      chat identifier
     * @param pageSize    number of messages to return
     * @param includeData if true, include retrieved data in the response, otherwise data field is null
     * @return chat
     * @throws EntityNotFoundException                                   chat of specified id not found
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @Transactional
    public ChatDto findById(UUID chatId, Integer pageSize, Boolean includeData) throws EntityNotFoundException {
        log.info("Get chat by id={}", chatId);

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(chat.getDatabase().getUser().getId());

        return ChatDto.builder()
                .id(chat.getId())
                .name(chat.getName())
                .modificationDate(chat.getModificationDate())
                .databaseId(chat.getDatabase().getId())
                .messages(chat.getMessages()
                        .stream()
                        .map(message -> {
                            String plotFileUrl = message.plotGenerated()
                                    ? PlotService.createFileUrl(chat.getId(), message.getId())
                                    : null;

                            RetrievedData data = includeData
                                    ? messageDataDAO.retrieveDataFromMessage(
                                            message, chat.getDatabase(), 0, pageSize)
                                    : null;

                            return new ChatResponse(data, message, plotFileUrl);
                        })
                        .toList())
                .build();
    }

    /**
     * Find all chats associated with specified database sorted by the modification date in descending order.
     *
     * @param databaseId database identifier
     * @return list of chats
     * @throws EntityNotFoundException                                   database of specified identifier not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the database
     */
    public List<ChatHistoryItem> findChatsByDatabaseId(UUID databaseId) throws EntityNotFoundException {
        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUser().getId());

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
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the database
     */
    public ChatDto create(UUID databaseId) throws EntityNotFoundException {
        log.info("Create new chat.");

        Database database = databaseRepository.findById(databaseId)
                .orElseThrow(() -> new EntityNotFoundException(DATABASE, databaseId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(database.getUser().getId());

        Chat chat = Chat.builder()
                .name(NEW_CHAT_NAME)
                .modificationDate(Timestamp.from(Instant.now()))
                .database(database)
                .build();

        chat = chatRepository.save(chat);

        return new ChatDto(
                chat.getId(), chat.getName(), List.of(), chat.getModificationDate(), chat.getDatabase().getId());
    }

    /**
     * Add empty message to chat to get its ID before processing.
     *
     * @param chatId chat identifier
     * @return created empty message
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @Transactional
    public ChatQueryWithResponse addEmptyMessageToChat(UUID chatId)
            throws EntityNotFoundException {

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(chat.getDatabase().getUser().getId());

        Timestamp timestamp = Timestamp.from(Instant.now());
        ChatQueryWithResponse message = ChatQueryWithResponse.builder()
                .chat(chat)
                .build();

        chat.addMessage(message);
        chat.setModificationDate(timestamp);

        chatRepository.save(chat);
        return messageRepository.save(message);
    }

    /**
     * Rename chat. If the new name is longer than {@code CHAT_NAME_MAX_LENGTH},
     * use only the first {@code CHAT_NAME_MAX_LENGTH} characters
     *
     * @param chatId chat identifier
     * @param name   new name
     * @throws EntityNotFoundException                                   chat of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    public void renameChat(UUID chatId, String name) throws EntityNotFoundException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(chat.getDatabase().getUser().getId());

        chat.setName(name.length() < CHAT_NAME_MAX_LENGTH ? name : name.substring(0, CHAT_NAME_MAX_LENGTH));
        chatRepository.save(chat);
    }

    /**
     * Delete chat by id and associated graph if it exists.
     *
     * @param chatId chat identifier
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    public void deleteChatById(UUID chatId) {
        Optional<Chat> chat = chatRepository.findById(chatId);

        if (chat.isPresent()) {
            authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(chat.get().getDatabase().getUser().getId());
            chatRepository.deleteById(chatId);
            plotService.deletePlots(chatId.toString());
        }
    }
}
