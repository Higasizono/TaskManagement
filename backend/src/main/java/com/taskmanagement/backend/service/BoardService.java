package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
import com.taskmanagement.backend.dto.ColumnResponse;
import com.taskmanagement.backend.dto.CreateBoardRequest;
import com.taskmanagement.backend.dto.CreateCardRequest;
import com.taskmanagement.backend.dto.MoveCardRequest;
import com.taskmanagement.backend.dto.UpdateCardRequest;
import com.taskmanagement.backend.entity.Board;
import com.taskmanagement.backend.entity.BoardColumn;
import com.taskmanagement.backend.entity.Card;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.exception.CardNotFoundException;
import com.taskmanagement.backend.exception.ColumnNotFoundException;
import com.taskmanagement.backend.repository.BoardColumnRepository;
import com.taskmanagement.backend.repository.BoardRepository;
import com.taskmanagement.backend.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    public void deleteBoard(UUID boardId) {
        if (!boardRepository.existsById(boardId)) {
            throw new BoardNotFoundException(boardId);
        }
        boardRepository.deleteById(boardId);
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

    @Transactional
    public CardResponse updateCardTitle(UUID boardId, UUID columnId, UUID cardId, UpdateCardRequest request) {
        Card card = getCardOrThrow(boardId, columnId, cardId);
        card.rename(request.title());
        return CardResponse.from(card);
    }

    @Transactional
    public CardResponse moveCard(UUID boardId, UUID columnId, UUID cardId, MoveCardRequest request) {
        Card card = getCardOrThrow(boardId, columnId, cardId);
        UUID targetColumnId = request.targetColumnId();

        if (columnId.equals(targetColumnId)) {
            reorderWithinColumn(card, columnId, request.targetIndex());
        } else {
            BoardColumn targetColumn = boardColumnRepository.findById(targetColumnId)
                    .filter(c -> c.getBoard().getId().equals(boardId))
                    .orElseThrow(() -> new ColumnNotFoundException(targetColumnId));
            moveAcrossColumns(card, columnId, targetColumn, request.targetIndex());
        }

        return CardResponse.from(card);
    }

    private Card getCardOrThrow(UUID boardId, UUID columnId, UUID cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException(cardId));
        if (!card.getColumn().getId().equals(columnId)
                || !card.getColumn().getBoard().getId().equals(boardId)) {
            throw new CardNotFoundException(cardId);
        }
        return card;
    }

    private void reorderWithinColumn(Card card, UUID columnId, int targetIndex) {
        List<Card> siblings = new ArrayList<>(cardRepository.findByColumnIdOrderByOrderIndexAsc(columnId));
        siblings.removeIf(c -> c.getId().equals(card.getId()));

        int clampedIndex = Math.max(0, Math.min(targetIndex, siblings.size()));
        siblings.add(clampedIndex, card);

        for (int i = 0; i < siblings.size(); i++) {
            Card sibling = siblings.get(i);
            if (sibling.getOrderIndex() != i) {
                sibling.updateOrderIndex(i);
            }
        }
        cardRepository.saveAll(siblings);
    }

    private void moveAcrossColumns(Card card, UUID sourceColumnId, BoardColumn targetColumn, int targetIndex) {
        List<Card> sourceSiblings = new ArrayList<>(cardRepository.findByColumnIdOrderByOrderIndexAsc(sourceColumnId));
        sourceSiblings.removeIf(c -> c.getId().equals(card.getId()));
        for (int i = 0; i < sourceSiblings.size(); i++) {
            Card sibling = sourceSiblings.get(i);
            if (sibling.getOrderIndex() != i) {
                sibling.updateOrderIndex(i);
            }
        }
        cardRepository.saveAll(sourceSiblings);

        List<Card> targetSiblings = new ArrayList<>(cardRepository.findByColumnIdOrderByOrderIndexAsc(targetColumn.getId()));
        int clampedIndex = Math.max(0, Math.min(targetIndex, targetSiblings.size()));
        targetSiblings.add(clampedIndex, card);

        for (int i = 0; i < targetSiblings.size(); i++) {
            Card sibling = targetSiblings.get(i);
            if (sibling.getId().equals(card.getId())) {
                card.moveTo(targetColumn, i);
            } else if (sibling.getOrderIndex() != i) {
                sibling.updateOrderIndex(i);
            }
        }
        cardRepository.saveAll(targetSiblings);
    }
}
