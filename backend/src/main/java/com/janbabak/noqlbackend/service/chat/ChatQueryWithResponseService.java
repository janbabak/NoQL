package com.janbabak.noqlbackend.service.chat;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.database.MessageDataDAO;
import com.janbabak.noqlbackend.service.langchain.QueryDatabaseLLMService;
import com.janbabak.noqlbackend.service.user.AuthenticationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
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

    public ChatQueryWithResponse findById(UUID messageId) throws EntityNotFoundException {
        log.info("Get message by id={}.", messageId);

        return chatQueryWithResponseRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(MESSAGE, messageId));
    }

    /**
     * Retrieve all messages belonging to the chat.
     *
     * @param chatId identifier of the chat
     * @return Messages sorted from the oldest to the most recent.
     * @throws EntityNotFoundException chat not found
     */
    public List<ChatQueryWithResponse> getMessagesFromChat(UUID chatId) throws EntityNotFoundException {
        final Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

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

        final ChatQueryWithResponse message = chatQueryWithResponseRepository.findById(messageId)
                .orElseThrow(() -> new EntityNotFoundException(MESSAGE, messageId));

        authenticationService.ifNotAdminOrSelfRequestThrowAccessDenied(message.getChat().getDatabase().getUserId());

        return messageDataDAO.retrieveDataFromMessage(message, message.getChat().getDatabase(), page, pageSize);
    }

    /**
     * Update empty message that was created just to get ID
     */
    @Transactional
    public ChatQueryWithResponse updateEmptyMessage( // TODO: test
            ChatQueryWithResponse message, String nlQuery, QueryDatabaseLLMService.LLMServiceResult llmResult) {

        final Timestamp timestamp = Timestamp.from(Instant.now());

        message.setNlQuery(nlQuery);
        message.setResultDescription(llmResult.llmResponse());
        message.setDbQuery(llmResult.toolResult().getDbQuery());
        message.setDbQueryExecutionSuccess(llmResult.toolResult().getDbQueryExecutedSuccessSuccessfully());
        message.setDbExecutionErrorMessage(llmResult.toolResult().getDbQueryExecutionErrorMessage());
        message.setPlotScript(llmResult.toolResult().getScript());
        message.setPlotGenerationSuccess(llmResult.toolResult().getPlotGeneratedSuccessfully());
        message.setPlotGenerationErrorMessage(llmResult.toolResult().getPlotGenerationErrorMessage());
        message.setTimestamp(timestamp);

        message.getChat().setModificationDate(timestamp);

        return chatQueryWithResponseRepository.save(message);
    }
}
