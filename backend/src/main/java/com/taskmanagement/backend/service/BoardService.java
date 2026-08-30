package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
import com.taskmanagement.backend.dto.ColumnResponse;
import com.taskmanagement.backend.dto.CreateBoardRequest;
import com.taskmanagement.backend.dto.CreateCardRequest;
import com.taskmanagement.backend.dto.UpdateBoardRequest;
import com.taskmanagement.backend.entity.Board;
import com.taskmanagement.backend.entity.BoardColumn;
import com.taskmanagement.backend.entity.Card;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.exception.ColumnNotFoundException;
import com.taskmanagement.backend.repository.BoardColumnRepository;
import com.taskmanagement.backend.repository.BoardRepository;
import com.taskmanagement.backend.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@Transactional(readOnly = true)
public class BoardService {

    private static final List<String> DEFAULT_COLUMN_TITLES = List.of("未着手", "進行中", "完了");

    private final BoardRepository boardRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final CardRepository cardRepository;

    public BoardService(
            BoardRepository boardRepository,
            BoardColumnRepository boardColumnRepository,
            CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.cardRepository = cardRepository;
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

    @Transactional
    public BoardSummaryResponse createBoard(CreateBoardRequest request) {
        Board board = new Board(request.title());
        boardRepository.save(board);

        List<BoardColumn> columns = IntStream.range(0, DEFAULT_COLUMN_TITLES.size())
                .mapToObj(index -> new BoardColumn(board, DEFAULT_COLUMN_TITLES.get(index), index))
                .toList();
        boardColumnRepository.saveAll(columns);

        return BoardSummaryResponse.from(board);
    }

    @Transactional
    public BoardSummaryResponse updateBoard(UUID boardId, UpdateBoardRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException(boardId));

        board.updateTitle(request.title());
        boardRepository.save(board);

        return BoardSummaryResponse.from(board);
    }

    @Transactional
    public CardResponse createCard(UUID boardId, UUID columnId, CreateCardRequest request) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .filter(c -> c.getBoard().getId().equals(boardId))
                .orElseThrow(() -> new ColumnNotFoundException(columnId));

        int nextOrderIndex = cardRepository.findMaxOrderIndexByColumnId(columnId) + 1;
        Card card = new Card(column, request.title(), nextOrderIndex);
        cardRepository.save(card);

        return CardResponse.from(card);
    }
}
