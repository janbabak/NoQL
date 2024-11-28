package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.query.llama.ChatResponseData;
import com.janbabak.noqlbackend.service.QueryService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageController {

    private final QueryService queryService;

    @GetMapping("/{messageId}/data")
    public ChatResponseData loadChatResponseData(
            @PathVariable UUID messageId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize
    ) throws DatabaseConnectionException, BadRequestException, EntityNotFoundException {
        return queryService.loadChatResponseData(messageId, page, pageSize);
    }
}
