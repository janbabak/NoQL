package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.chat.ChatDto;
import com.janbabak.noqlbackend.model.chat.CreateChatQueryWithResponseRequest;
import com.janbabak.noqlbackend.service.ChatService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
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
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatDto create(@RequestParam UUID databaseId) throws EntityNotFoundException {
        return chatService.create(databaseId);
    }

    /**
     * Get chat by id
     *
     * @param chatId chat identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @GetMapping("/{chatId}")
    public ChatDto getById(@PathVariable UUID chatId) throws EntityNotFoundException {
        return chatService.findById(chatId);
    }

    /**
     * Delete chat by id.
     * @param chatId chat identifier
     */
    @DeleteMapping("/{chatId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable UUID chatId) {
        chatService.deleteChatById(chatId);
    }

    // TODO: name max length
    /**
     * Rename chat.
     * @param chatId chat identifier
     * @param name new name
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @PutMapping("/{chatId}/name")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void rename(@PathVariable UUID chatId, @RequestParam @Valid @NotEmpty String name)
            throws EntityNotFoundException {
        chatService.renameChat(chatId, name);
    }

    /**
     * Add message to a chat. Does not verify if the JSON is valid.
     *
     * @param chatId      chat identifier
     * @param request message
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @PostMapping("/{chatId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMessage(@PathVariable UUID chatId, @RequestBody @Valid CreateChatQueryWithResponseRequest request)
            throws EntityNotFoundException {
        chatService.addMessageToChat(chatId, request);
        System.out.println("ahoj");
    }
}
