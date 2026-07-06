package com.taskmanagement.backend.repository;

import com.taskmanagement.backend.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {

    List<Board> findAllByOrderByCreatedAtAsc();
}
