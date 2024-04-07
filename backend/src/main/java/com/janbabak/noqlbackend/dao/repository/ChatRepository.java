package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Chat;
import com.janbabak.noqlbackend.model.entity.Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Automatically generated CRUD and other methods.
 */
@Repository
public interface ChatRepository extends JpaRepository<Chat, UUID> {

    /**
     * Find all chats associated with a database.
     * @param database database object
     * @return list of chats associated with specified database.
     */
    List<Chat> findAllByDatabase(Database database);
}
