package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.CustomModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Automatically generated CRUD and other methods.
 */
@Repository
public interface CustomModelRepository extends JpaRepository<CustomModel, UUID> {

    /**
     * Get all databases owned by user
     * @param userId user id
     * @return databases filtered by user id
     */
    @Query("SELECT m FROM CustomModel m WHERE m.user.id = :userId")
    List<CustomModel> findAllByUserId(@Param("userId") UUID userId);
}
