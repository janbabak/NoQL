package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Automatically generated CRUD and other methods.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {
}
