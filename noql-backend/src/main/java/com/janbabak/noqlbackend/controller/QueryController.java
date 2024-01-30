package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.error.exception.LLMException;
import com.janbabak.noqlbackend.model.QueryRequest;
import com.janbabak.noqlbackend.model.database.QueryResponse;
import com.janbabak.noqlbackend.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

/**
 * Responsible for querying the user's database.
 */
@RestController
@RequestMapping(value = "/query", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class QueryController {

    private final QueryService queryService;

    /**
     * Query the user's database.
     *
     * @param request body
     * @return query result
     * @throws EntityNotFoundException queried database not found.
     * @throws LLMException            LLM request failed.
     * @throws SQLException            TODO
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse queryDatabase(@RequestBody QueryRequest request)
            throws SQLException, LLMException, EntityNotFoundException {
        return queryService.handleQuery(request);
    }
}
