package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDtoNew;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Create new chat
     *
     * @param databaseId database identifier
     * @return created object with its id.
     * @throws EntityNotFoundException database of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the database
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDtoNew create(@RequestParam UUID databaseId) throws EntityNotFoundException {
        return chatService.create(databaseId);
    }

    /**
     * Get chat by id
     *
     * @param chatId chat identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @GetMapping("/{chatId}") // TODO: test
    public ChatDtoNew getByIdNew(@PathVariable UUID chatId, @RequestParam(required = false) Integer pageSize)
            throws EntityNotFoundException {
        return chatService.findByIdNew(chatId, pageSize, true);
    }

    /**
     * Delete chat by id.
     *
     * @param chatId chat identifier
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID chatId) {
        chatService.deleteChatById(chatId);
    }

    /**
     * Rename chat.
     *
     * @param chatId chat identifier
     * @param name   new name
     * @throws EntityNotFoundException                                   chat of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @PutMapping("/{chatId}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rename(@PathVariable UUID chatId, @RequestParam @Valid @Length(min = 1, max = 32) String name)
            throws EntityNotFoundException {
        chatService.renameChat(chatId, name);
    }

    /**
     * Add message to a chat. Does not verify if the JSON is valid.
     *
     * @param chatId  chat identifier
     * @param request message
     * @throws EntityNotFoundException                                   chat of specified id not found.
     * @throws org.springframework.security.access.AccessDeniedException if the user is not the owner of the chat
     */
    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMessage(@PathVariable UUID chatId, @RequestBody @Valid CreateChatQueryWithResponseRequest request)
            throws EntityNotFoundException {
        chatService.addMessageToChat(chatId, request);
    }
}
