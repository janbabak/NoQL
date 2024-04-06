package com.janbabak.noqlbackend.dao.repository;

import com.janbabak.noqlbackend.model.entity.MessageWithResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageWithResponseRepository extends JpaRepository<MessageWithResponse, UUID> {
}
