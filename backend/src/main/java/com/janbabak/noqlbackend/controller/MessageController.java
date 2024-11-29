package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.query.ChatResponseData;
import com.janbabak.noqlbackend.service.QueryService;
import lombok.RequiredArgsConstructor;
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
            @RequestParam(required = false) Integer pageSize) throws EntityNotFoundException {
        return queryService.loadChatResponseData(messageId, page, pageSize);
    }
}
