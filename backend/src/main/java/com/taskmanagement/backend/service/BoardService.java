package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.ColumnResponse;
import com.taskmanagement.backend.entity.Board;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.repository.BoardColumnRepository;
import com.taskmanagement.backend.repository.BoardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;

    public BoardService(BoardRepository boardRepository, BoardColumnRepository boardColumnRepository) {
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
    }

    public List<BoardSummaryResponse> getAllBoards() {
        return boardRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(BoardSummaryResponse::from)
                .toList();
    }

    public BoardDetailResponse getBoardDetail(UUID boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        List<ColumnResponse> columns = boardColumnRepository.findByBoardIdWithCards(boardId).stream()
                .map(ColumnResponse::from)
                .toList();

        return BoardDetailResponse.from(board, columns);
    }
}
