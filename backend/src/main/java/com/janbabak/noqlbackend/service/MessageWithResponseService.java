package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.dao.repository.ChatRepository;
import com.janbabak.noqlbackend.dao.repository.MessageWithResponseRepository;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.MessageWithResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.janbabak.noqlbackend.error.exception.EntityNotFoundException.Entity.CHAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageWithResponseService {

    private final MessageWithResponseRepository messageWithResponseRepository;
    private final ChatRepository chatRepository;

    public List<MessageWithResponse> getMessagesFromChat(UUID chatId) throws EntityNotFoundException {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new EntityNotFoundException(CHAT, chatId));

        return messageWithResponseRepository.findAllByChatOrderByTimestamp(chat);
    }
}
