package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.BoardColumn;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {

    @Query(
            "SELECT bc FROM BoardColumn bc LEFT JOIN FETCH bc.cards WHERE bc.board.id = :boardId ORDER BY bc.orderIndex ASC")
    List<BoardColumn> findByBoardIdWithCards(@Param("boardId") UUID boardId);
}
