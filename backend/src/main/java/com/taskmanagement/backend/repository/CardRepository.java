package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT COALESCE(MAX(c.orderIndex), -1) FROM Card c WHERE c.column.id = :columnId")
    int findMaxOrderIndexByColumnId(@Param("columnId") UUID columnId);
}
