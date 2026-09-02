package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.Card;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardRepository extends JpaRepository<Card, UUID> {

    @Query("SELECT COALESCE(MAX(c.orderIndex), -1) FROM Card c WHERE c.column.id = :columnId")
    int findMaxOrderIndexByColumnId(@Param("columnId") UUID columnId);

    @Query("SELECT c FROM Card c WHERE c.column.id = :columnId ORDER BY c.orderIndex ASC")
    List<Card> findByColumnIdOrderByOrderIndexAsc(@Param("columnId") UUID columnId);
}
