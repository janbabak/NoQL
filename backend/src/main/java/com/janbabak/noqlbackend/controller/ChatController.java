package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * Get chat by id
     *
     * @param id chat identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @GetMapping("/{id}")
    public ChatDto getById(@PathVariable UUID id) throws EntityNotFoundException {
        return chatService.findById(id);
    }

    /**
     * Delete chat by id.
     * @param id chat identifier
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteById(@PathVariable UUID id) {
        chatService.deleteChatById(id);
    }

    /**
     * Create new chat
     *
     * @param databaseId database identifier
     * @return created object with its id.
     * @throws EntityNotFoundException database of specified id not found.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDto create(@RequestParam UUID databaseId) throws EntityNotFoundException {
        return chatService.create(databaseId);
    }

    /**
     * Rename chat.
     * @param id chat identifier
     * @param name new name
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @PutMapping("/{id}/name")
    @ResponseStatus(HttpStatus.OK)
    public void rename(@PathVariable UUID id, @RequestParam @Valid @NotEmpty String name)
            throws EntityNotFoundException {
        chatService.renameChat(id, name);
    }

    /**
     * Add message to a chat
     *
     * @param id      chat identifier
     * @param request message
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @PostMapping("/{id}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMessage(@PathVariable UUID id, @RequestBody @Valid CreateChatQueryWithResponseRequest request)
            throws EntityNotFoundException {
        chatService.addMessageToChat(id, request);
    }
}
