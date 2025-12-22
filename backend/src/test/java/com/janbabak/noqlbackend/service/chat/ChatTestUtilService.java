package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;
import static com.janbabak.noqlbackend.service.chat.ChatService.CHAT_NAME_MAX_LENGTH;
import static com.janbabak.noqlbackend.service.chat.ChatService.NEW_CHAT_NAME;

@Service
@AllArgsConstructor
public class ChatTestUtilService {

    private final ChatRepository chatRepository;
    private final ChatQueryWithResponseRepository messageRepository;
    private final AuthenticationService authenticationService;

    /**
     * Add message to chat
     *
     * @param chatId  chat identifier
     * @param message message to add
     * @throws EntityNotFoundException                                   chat of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @Transactional
    public void addMessageToChat(UUID chatId, ChatQueryWithResponse message)
            throws EntityNotFoundException {

        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(chat.getDatabase().getUser().getId());

        Timestamp timestamp = Timestamp.from(Instant.now());
        message.setTimestamp(timestamp);
        message.setChat(chat);
        chat.addMessage(message);
        chat.setModificationDate(timestamp);

        if (Objects.equals(chat.getName(), NEW_CHAT_NAME)) {
            chat.setName(message.getNlQuery().length() < CHAT_NAME_MAX_LENGTH
                    ? message.getNlQuery()
                    : message.getNlQuery().substring(0, CHAT_NAME_MAX_LENGTH));
        }
        chatRepository.save(chat);
        messageRepository.save(message);
    }
}
