package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.ChatQueryWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatQueryWithResponseService {

    private final ChatQueryWithResponseRepository chatQueryWithResponseRepository;
    private final ChatRepository chatRepository;

    public List<ChatQueryWithResponse> getMessagesFromChat(UUID chatId) throws EntityNotFoundException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        return chatQueryWithResponseRepository.findAllByChatOrderByTimestamp(chat);
    }
}
