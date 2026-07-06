package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.entity.Board;
import com.taskmanagement.backend.entity.BoardColumn;
import com.taskmanagement.backend.entity.Card;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.repository.BoardColumnRepository;
import com.taskmanagement.backend.repository.BoardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    private BoardService boardService;

    private BoardService service() {
        return new BoardService(boardRepository, boardColumnRepository);
    }

    private Board buildBoard(UUID id, String title) {
        Board board = new Board();
        ReflectionTestUtils.setField(board, "id", id);
        ReflectionTestUtils.setField(board, "title", title);
        ReflectionTestUtils.setField(board, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(board, "updatedAt", OffsetDateTime.now());
        return board;
    }

    private BoardColumn buildColumn(UUID id, String title, int orderIndex, List<Card> cards) {
        BoardColumn column = new BoardColumn();
        ReflectionTestUtils.setField(column, "id", id);
        ReflectionTestUtils.setField(column, "title", title);
        ReflectionTestUtils.setField(column, "orderIndex", orderIndex);
        ReflectionTestUtils.setField(column, "cards", cards);
        return column;
    }

    private Card buildCard(UUID id, String title, int orderIndex) {
        Card card = new Card();
        ReflectionTestUtils.setField(card, "id", id);
        ReflectionTestUtils.setField(card, "title", title);
        ReflectionTestUtils.setField(card, "orderIndex", orderIndex);
        ReflectionTestUtils.setField(card, "createdAt", OffsetDateTime.now());
        ReflectionTestUtils.setField(card, "updatedAt", OffsetDateTime.now());
        return card;
    }

    @Test
    void getAllBoards_returnsBoardsMappedToSummaryResponses() {
        boardService = service();
        Board board = buildBoard(UUID.randomUUID(), "スクール課題");
        when(boardRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(board));

        List<BoardSummaryResponse> result = boardService.getAllBoards();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("スクール課題");
    }

    @Test
    void getBoardDetail_returnsColumnsAndCardsInOrder() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        Card card = buildCard(UUID.randomUUID(), "READMEを書く", 0);
        BoardColumn column = buildColumn(UUID.randomUUID(), "未着手", 0, List.of(card));

        when(boardRepository.findById(boardId)).thenReturn(Optional.of(board));
        when(boardColumnRepository.findByBoardIdWithCards(boardId)).thenReturn(List.of(column));

        BoardDetailResponse result = boardService.getBoardDetail(boardId);

        assertThat(result.title()).isEqualTo("個人開発");
        assertThat(result.columns()).hasSize(1);
        assertThat(result.columns().get(0).cards()).hasSize(1);
        assertThat(result.columns().get(0).cards().get(0).title()).isEqualTo("READMEを書く");
    }

    @Test
    void getBoardDetail_throwsWhenBoardNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        when(boardRepository.findById(boardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.getBoardDetail(boardId))
                .isInstanceOf(BoardNotFoundException.class);
    }
}
