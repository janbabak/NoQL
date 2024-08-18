package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.CustomModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Automatically generated CRUD and other methods.
 */
@Repository
public interface CustomModelRepository extends JpaRepository<CustomModel, UUID> {
}
