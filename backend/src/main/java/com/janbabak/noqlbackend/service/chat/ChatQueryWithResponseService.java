package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;
import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.MESSAGE;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatQueryWithResponseService {

    private final AuthenticationService authenticationService;
    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;
    private final ChatRepository chatRepository;
    private final MessageDataDAO messageDataDAO;

    /**
     * Retrieve all messages belonging to the chat.
     *
     * @param chatId identifier of the chat
     * @return Messages sorted from the oldest to the most recent.
     * @throws EntityNotFoundException chat not found
     */
    public List<ChatQueryWithResponse> getMessagesFromChat(UUID chatId) throws EntityNotFoundException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        return chatQueryWithResponseRepository.findAllByChatOrderByTimestamp(chat);
    }

    /**
     * Load result of response of last message from chat. Used when user opens an old chat.
     *
     * @param messageId identifier of the message to load
     * @param page      page number (first pages has is 0)
     * @param pageSize  number of items per page
     * @return query response
     * @throws EntityNotFoundException database or chat not found
     */
    public RetrievedData getDataByMessageId(UUID messageId, Integer page, Integer pageSize)
            throws EntityNotFoundException {

        ChatQueryWithResponse message = chatQueryWithResponseRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(MESSAGE, messageId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(message.getChat().getDatabase().getUserId());

        return messageDataDAO.retrieveDataFromMessage(message, message.getChat().getDatabase(), page, pageSize);
    }
}
