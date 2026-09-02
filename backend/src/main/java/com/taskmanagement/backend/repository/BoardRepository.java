package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.Board;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findAllByOrderByCreatedAtAsc();
}
