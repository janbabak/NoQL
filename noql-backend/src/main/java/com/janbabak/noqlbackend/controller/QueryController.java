package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.model.QueryRequest;
import com.janbabak.noqlbackend.model.database.QueryResponse;
import com.janbabak.noqlbackend.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

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
     * @param request body
     * @return query result
     * @throws Exception when something went wrong
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public QueryResponse queryDatabase(@RequestBody QueryRequest request) throws Exception {
        return queryService.handleQuery(request);
    }
}
