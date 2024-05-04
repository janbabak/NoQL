package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Automatically generated CRUD and other methods.
 */
@Repository
public interface ChatQueryWithResponseRepository extends JpaRepository<ChatQueryWithResponse, UUID> {

    /**
     * Find all messages that relate to the chat ordered from the oldest to the most recent.
     * @param chat chat
     * @return list of messages
     */
    List<ChatQueryWithResponse> findAllByChatOrderByTimestamp(Chat chat);

    /**
     * Find the most recent message from chat.
     * @param chatId chat identifier
     * @return the most recent message if it exists
     */
    @Query("SELECT message " +
            "FROM ChatQueryWithResponse message " +
            "WHERE message.chat.id = :chatId " +
            "ORDER BY message.timestamp DESC " +
            "LIMIT 1")
    Optional<ChatQueryWithResponse> findLatestMessageFromChat(@Param("chatId") UUID chatId);

}
