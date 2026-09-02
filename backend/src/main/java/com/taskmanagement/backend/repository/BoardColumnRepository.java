package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.BoardColumn;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardColumnRepository extends JpaRepository<BoardColumn, UUID> {

    // カラム内のカード順は @OrderBy の暗黙適用に頼らず ORDER BY で明示する。
    @Query(
            """
            SELECT bc FROM BoardColumn bc
            LEFT JOIN FETCH bc.cards c
            WHERE bc.board.id = :boardId
            ORDER BY bc.orderIndex ASC, c.orderIndex ASC
            """)
    List<BoardColumn> findByBoardIdWithCards(@Param("boardId") UUID boardId);
}
