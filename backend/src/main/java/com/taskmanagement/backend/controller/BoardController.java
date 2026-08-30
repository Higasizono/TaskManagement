package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
import com.taskmanagement.backend.dto.CreateBoardRequest;
import com.taskmanagement.backend.dto.CreateCardRequest;
import com.taskmanagement.backend.dto.MoveCardRequest;
import com.taskmanagement.backend.dto.UpdateCardRequest;
import com.taskmanagement.backend.service.BoardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BoardSummaryResponse createBoard(@Valid @RequestBody CreateBoardRequest request) {
        return boardService.createBoard(request);
    }

    @PostMapping("/{boardId}/columns/{columnId}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public CardResponse createCard(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @Valid @RequestBody CreateCardRequest request) {
        return boardService.createCard(boardId, columnId, request);
    }

    @PatchMapping("/{boardId}/columns/{columnId}/cards/{cardId}")
    public CardResponse updateCard(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @PathVariable UUID cardId,
            @Valid @RequestBody UpdateCardRequest request) {
        return boardService.updateCardTitle(boardId, columnId, cardId, request);
    }

    @PatchMapping("/{boardId}/columns/{columnId}/cards/{cardId}/move")
    public CardResponse moveCard(
            @PathVariable UUID boardId,
            @PathVariable UUID columnId,
            @PathVariable UUID cardId,
            @Valid @RequestBody MoveCardRequest request) {
        return boardService.moveCard(boardId, columnId, cardId, request);
    }
}
