package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Automatically generates CRUD and other methods.
 */
@Repository
public interface DatabaseRepository extends JpaRepository<Database, UUID> {
}
