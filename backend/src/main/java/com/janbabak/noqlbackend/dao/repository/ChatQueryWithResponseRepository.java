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

@Repository
public interface ChatQueryWithResponseRepository extends JpaRepository<ChatQueryWithResponse, UUID> {

    List<ChatQueryWithResponse> findAllByChatOrderByTimestamp(Chat chat);

    // TODO: test
    @Query("SELECT message " +
            "FROM ChatQueryWithResponse message " +
            "WHERE message.chat.id = :chatId " +
            "ORDER BY message.timestamp DESC " +
            "LIMIT 1")
    Optional<ChatQueryWithResponse> findLatestMessageFromChat(@Param("chatId") UUID chatId);

}
