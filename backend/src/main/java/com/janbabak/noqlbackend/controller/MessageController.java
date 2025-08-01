package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.query.RetrievedData;
import com.janbabak.noqlbackend.service.chat.ChatQueryWithResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/message", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageController {

    private final ChatQueryWithResponseService chatQueryWithResponseService;

    /**
     * Load data of specified message
     *
     * @param messageId message identifier
     * @param page      page number (starting from 0)
     * @param pageSize  number of rows on one page
     * @return data of the message
     * @throws EntityNotFoundException                                   when the message is not found
     * @throws org.springframework.security.access.AccessDeniedException when user is not admin or owner of the message
     */
    @GetMapping("/{messageId}/data")
    public RetrievedData loadMessageData(
                                             @PathVariable UUID messageId,
                                             @RequestParam(required = false) Integer page,
                                             @RequestParam(required = false) Integer pageSize)
            throws EntityNotFoundException {
        return chatQueryWithResponseService.getDataByMessageId(messageId, page, pageSize);
    }
}
