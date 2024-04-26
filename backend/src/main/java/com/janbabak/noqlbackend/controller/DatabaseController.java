package com.janbabak.noqlbackend.controller;


import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.DatabaseExecutionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.chat.ChatHistoryItem;
import com.janbabak.noqlbackend.model.chat.ChatResponse;
import com.janbabak.noqlbackend.model.database.*;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.model.query.QueryRequest;
import com.janbabak.noqlbackend.model.query.QueryResponse;
import com.janbabak.noqlbackend.service.ChatService;
import com.janbabak.noqlbackend.service.QueryService;
import com.janbabak.noqlbackend.service.database.DatabaseEntityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/database", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DatabaseController {

    private final DatabaseEntityService databaseService;
    private final QueryService queryService;
    private final ChatService chatService;

    /**
     * Get all databases.
     *
     * @return list of databases
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Database> getAll() {
        return databaseService.findAll();
    }

    /**
     * Get database by id.
     *
     * @param id database identifier
     * @return database
     * @throws EntityNotFoundException database of specified id not found.
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database getById(@PathVariable UUID id) throws EntityNotFoundException {
        return databaseService.findById(id);
    }

    /**
     * Create new database.
     *
     * @param request database object (without id)
     * @return created object with its id.
     * @throws DatabaseConnectionException connection to the database failed (wrong credential, unavailable, etc.)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Database create(@RequestBody @Valid Database request) throws DatabaseConnectionException {
        return databaseService.create(request);
    }

    /**
     * Update database by id - set non-null fields.
     *
     * @param id      database identifier
     * @param request new data
     * @return updated database
     * @throws EntityNotFoundException     database of specified id not found.
     * @throws DatabaseConnectionException connection to the updated database failed.
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Database update(@PathVariable UUID id, @RequestBody @Valid UpdateDatabaseRequest request)
            throws EntityNotFoundException, DatabaseConnectionException {
        return databaseService.update(id, request);
    }

    /**
     * Delete database by id
     *
     * @param id database identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID id) {
        databaseService.deleteById(id);
    }

    /**
     * Query the user's database using natural language from in chat form, result is automatically paginated.
     *
     * @param id          database id
     * @param queryRequest chat containing the natural language queries
     * @param pageSize    number of items in one page
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws LLMException                LLM request failed.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws DatabaseExecutionException  query execution failed (syntax error)
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    @PostMapping("/{id}/query/chat")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeChat(
            @PathVariable UUID id,
            @RequestBody @Valid QueryRequest queryRequest,
            @RequestParam Integer pageSize
    ) throws DatabaseConnectionException, DatabaseExecutionException,
            EntityNotFoundException, LLMException, BadRequestException {
        return queryService.executeChat(id, queryRequest, pageSize);
    }

    /**
     * EXPERIMENTAL
     */
    @PostMapping("/{id}/query/chatExperimental")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeChatExperimental(
            @PathVariable UUID id,
            @RequestBody @Valid QueryRequest queryRequest
    ) throws DatabaseConnectionException, DatabaseExecutionException,
            EntityNotFoundException, LLMException {
        return queryService.executeChatExperimental(id, queryRequest);
    }

    /**
     * Query the user's database using database query language, result is automatically paginated.
     *
     * @param id       database id
     * @param query    database query in corresponding database query language
     * @param page     page number (first pages is 0)
     * @param pageSize number of items in one page
     * @return query result
     * @throws EntityNotFoundException     queried database not found.
     * @throws DatabaseConnectionException cannot establish connection with the database
     * @throws BadRequestException         requested page size is greater than maximum allowed value
     */
    @PostMapping("/{id}/query/query-language")
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse executeQueryLanguageQuery(
            @PathVariable UUID id,
            @RequestBody String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {
        return queryService.executeQueryLanguageSelectQuery(id, query, page, pageSize);
    }

    /**
     * Get database structure by database id
     *
     * @param id identifier
     * @return database structure
     * @throws DatabaseConnectionException syntax error error, ...
     * @throws DatabaseExecutionException  connection to the database failed
     * @throws EntityNotFoundException     database of specific id not found
     */
    @GetMapping("/{id}/structure")
    @ResponseStatus(HttpStatus.OK)
    public DatabaseStructureDto getDatabaseStructure(@PathVariable UUID id)
            throws DatabaseConnectionException, DatabaseExecutionException, EntityNotFoundException {
        return databaseService.getDatabaseStructureByDatabaseId(id);
    }

    /**
     * Get chat history (chats associated to a specified database) sorted by the modification data in descending order.
     * @param id database identifier
     * @return list of chat DTOs
     * @throws EntityNotFoundException database of specified id not found.
     */
    @GetMapping("/{id}/chats")
    @ResponseStatus(HttpStatus.OK)
    public List<ChatHistoryItem> getChatHistoryByDatabaseId(@PathVariable UUID id) throws EntityNotFoundException {
        return chatService.findChatsByDatabaseId(id);
    }
}
