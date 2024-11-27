package com.janbabak.noqlbackend.controller;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.model.query.llama.ChatResponse;
import com.janbabak.noqlbackend.model.query.llama.ChatResponseData;
import com.janbabak.noqlbackend.service.chat.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import com.janbabak.noqlbackend.validation.ValidationSequence;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/database", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseEntityService databaseService;
    private final QueryService queryService;
    private final ChatService chatService;

    /**
     * Get all databases.
     *
     * @param userId user identifier
     * @return list of databases
     * @throws AccessDeniedException if user is not admin or owner of the database.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Database> getAll(@RequestParam(required = false) UUID userId) {
        return databaseService.findAll(userId);
    }

    /**
     * Get database by id.
     *
     * @param databaseId database identifier
     * @return database
     * @throws EntityNotFoundException database of specified id not found.
     * @throws AccessDeniedException   if user is not admin or owner of the database.
     */
    @GetMapping("/{databaseId}")
    @ResponseStatus(HttpStatus.OK)
    public Database getById(@PathVariable UUID databaseId) throws EntityNotFoundException {
        return databaseService.findById(databaseId);
    }

    /**
     * Create new database.
     *
     * @param request database object (without id)
     * @return created object with its id.
     * @throws DatabaseConnectionException connection to the database failed (wrong credential, unavailable, etc.)
     * @throws EntityNotFoundException     user not found
     * @throws AccessDeniedException       user is not admin or doesn't belong to userId
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Database create(@Validated(ValidationSequence.class) @RequestBody CreateDatabaseRequest request)
            throws DatabaseConnectionException, EntityNotFoundException {
        return databaseService.create(request);
    }

    /**
     * Update database by id - set non-null fields.
     *
     * @param databaseId database identifier
     * @param request    new data
     * @return updated database
     * @throws EntityNotFoundException     database of specified id not found.
     * @throws DatabaseConnectionException connection to the updated database failed.
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @PutMapping("/{databaseId}")
    @ResponseStatus(HttpStatus.OK)
    public Database update(@PathVariable UUID databaseId, @RequestBody @Valid UpdateDatabaseRequest request)
            throws EntityNotFoundException, DatabaseConnectionException {
        return databaseService.update(databaseId, request);
    }

    /**
     * Delete database by id
     *
     * @param id database identifier
     * @throws EntityNotFoundException user from database not found
     * @throws AccessDeniedException   if user is not admin or owner of the database.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) throws EntityNotFoundException {
        databaseService.deleteById(id);
    }

    /**
     * Query the user's database using natural language from in chat form.
     *
     * @param databaseId   database identifier
     * @param chatId       chat identifier
     * @param queryRequest query
     * @param pageSize     number of items in one page
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  retrieving database schema failure
     * @throws EntityNotFoundException     database not found
     * @throws LLMException                LLM request failed
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @Deprecated // TODO: remove
    @PostMapping("/{databaseId}/chat/{chatId}/query")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeChat(
            @PathVariable UUID databaseId,
            @PathVariable UUID chatId,
            @RequestBody @Valid QueryRequest queryRequest,
            @RequestParam Integer pageSize
    ) throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException,
            LLMException, BadRequestException {
        return queryService.executeChat(databaseId, chatId, queryRequest, pageSize);
    }

    /**
     * Query the user's database using natural language from in chat form.
     *
     * @param databaseId   database identifier
     * @param chatId       chat identifier
     * @param queryRequest query
     * @param pageSize     number of items in one page
     * @return query result
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  retrieving database schema failure
     * @throws EntityNotFoundException     database not found
     * @throws LLMException                LLM request failed
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @PostMapping("/{databaseId}/chat/{chatId}/queryNew")
    @ResponseStatus(HttpStatus.OK)
    public ChatResponse queryChat(
            @PathVariable UUID databaseId,
            @PathVariable UUID chatId,
            @RequestBody @Valid QueryRequest queryRequest,
            @RequestParam Integer pageSize
    ) throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException,
            LLMException, BadRequestException {
        return queryService.queryChat(databaseId, chatId, queryRequest, pageSize);
    }

    /**
     * Load chat result of specific message of existing chat.
     *
     * @param databaseId database identifier
     * @param chatId     chat identifier
     * @param messageId  message identifier
     * @param page       page number (fist page is 0)
     * @param pageSize   number of items per page
     * @return result of the latest message from the chat
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws EntityNotFoundException     database or chat not found
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @Deprecated // TODO: remove
    @GetMapping("/{databaseId}/chat/{chatId}/message/{messageId}")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse loadChatResult(
            @PathVariable UUID databaseId,
            @PathVariable UUID chatId,
            @PathVariable UUID messageId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {
        return queryService.loadChatResult(databaseId, chatId, messageId, page, pageSize);
    }

    /**
     * Load result of specific message of existing chat.
     *
     * @param databaseId database identifier
     * @param chatId     chat identifier
     * @param messageId  message identifier
     * @param page       page number (fist page is 0)
     * @param pageSize   number of items per page
     * @return result of the latest message from the chat
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws EntityNotFoundException     database or chat not found
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @GetMapping("/{databaseId}/chat/{chatId}/message/{messageId}/data")
    @ResponseStatus(HttpStatus.OK)
    public ChatResponseData loadChatResultNew(
            @PathVariable UUID databaseId,
            @PathVariable UUID chatId,
            @PathVariable UUID messageId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {
        return queryService.loadChatResultData(databaseId, chatId, messageId, page, pageSize);
    }

    /**
     * Query the user's database using database query language, result is automatically paginated.
     *
     * @param databaseId database identifier
     * @param query      database query in corresponding database query language
     * @param page       page number (first pages is 0)
     * @param pageSize   number of items in one page
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         pageSize value is greater than maximum allowed value
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @PostMapping(path = "/{databaseId}/query/queryLanguage", consumes = MediaType.TEXT_PLAIN_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeQueryLanguageQuery(
            @PathVariable UUID databaseId,
            @RequestBody String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {
        return queryService.executeQueryLanguageSelectQuery(databaseId, query, page, pageSize);
    }

    /**
     * Get database structure by database id
     *
     * @param databaseId database identifier
     * @return database structure
     * @throws DatabaseConnectionException connection to the database failed
     * @throws DatabaseExecutionException  syntax error, ...
     * @throws EntityNotFoundException     database of specific id not found
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @GetMapping("/{databaseId}/structure")
    @ResponseStatus(HttpStatus.OK)
    public DatabaseStructureDto getDatabaseStructure(@PathVariable UUID databaseId)
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {
        return databaseService.getDatabaseStructureByDatabaseId(databaseId);
    }

    /**
     * Get generated create script by database id
     *
     * @param id identifier
     * @return create script
     * @throws DatabaseConnectionException connection to the database failed
     * @throws DatabaseExecutionException  syntax error, ...
     * @throws EntityNotFoundException     database of specific id not found
     * @throws AccessDeniedException       if user is not admin or owner of the database.
     */
    @GetMapping(path = "/{id}/createScript", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getCreateScript(@PathVariable UUID id)
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {
        return databaseService.getDatabaseCreateScriptByDatabaseId(id);
    }

    /**
     * Get chat history (chats associated to a specified database) sorted by the modification data in descending order.
     *
     * @param databaseId database identifier
     * @return list of chat DTOs
     * @throws EntityNotFoundException database of specified id not found.
     * @throws AccessDeniedException   if user is not admin or owner of the database.
     */
    @GetMapping("/{databaseId}/chats")
    @ResponseStatus(HttpStatus.OK)
    public List<ChatHistoryItem> getChatHistoryByDatabaseId(@PathVariable UUID databaseId)
            throws EntityNotFoundException {
        return chatService.findChatsByDatabaseId(databaseId);
    }
}
