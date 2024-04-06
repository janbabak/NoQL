package com.janbabak.noqlbackend.controller;

import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.MessageWithResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(value = "/chat")
public class ChatController {

    /**
     * Get chat by id
     * @param id chat identifier
     * @return chat
     * @throws EntityNotFoundException chat of specified id not found.
     */
    @GetMapping("/{id}")
    public Chat getById(@PathVariable UUID id) throws EntityNotFoundException {
        return null; // TODO implement
    }

    /**
     * Create new chat
     * @param request chat object (without id)
     * @return created object with its id.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chat create(@RequestBody @Valid Chat request) {
        return null; // TODO implement
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public void addMessage(@RequestBody @Valid MessageWithResponse message) {
        // TODO implement
    }
}
