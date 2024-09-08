package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Automatically generates CRUD and other methods.
 */
@Repository
public interface DatabaseRepository extends JpaRepository<Database, UUID> {

    /**
     * Get all databases owned by user
     * @param userId user id
     * @return databases filtered by user id
     */
    @Query("SELECT d FROM Database d WHERE d.user.id = :userId")
    List<Database> findAllByUserId(@Param("userId") UUID userId);
}
