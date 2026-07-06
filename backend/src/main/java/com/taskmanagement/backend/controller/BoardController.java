package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.service.BoardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping
    public List<BoardSummaryResponse> getAllBoards() {
        return boardService.getAllBoards();
    }

    @GetMapping("/{boardId}")
    public BoardDetailResponse getBoardDetail(@PathVariable UUID boardId) {
        return boardService.getBoardDetail(boardId);
    }
}
