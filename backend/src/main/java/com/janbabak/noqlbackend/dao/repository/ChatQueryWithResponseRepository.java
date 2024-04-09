package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.ChatQueryWithResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChatQueryWithResponseRepository extends JpaRepository<ChatQueryWithResponse, UUID> {

    List<ChatQueryWithResponse> findAllByChatOrderByTimestamp(Chat chat);
}
