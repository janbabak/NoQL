package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.database.Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DatabaseRepository extends JpaRepository<Database, UUID> {
}
