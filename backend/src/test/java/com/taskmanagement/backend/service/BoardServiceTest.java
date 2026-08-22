package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
import com.taskmanagement.backend.dto.CreateBoardRequest;
import com.taskmanagement.backend.dto.CreateCardRequest;
import com.taskmanagement.backend.entity.Board;
import com.taskmanagement.backend.entity.BoardColumn;
import com.taskmanagement.backend.entity.Card;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.exception.ColumnNotFoundException;
import com.taskmanagement.backend.repository.BoardColumnRepository;
import com.taskmanagement.backend.repository.BoardRepository;
import com.taskmanagement.backend.repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private CardRepository cardRepository;

    private BoardService boardService;

    private BoardService service() {
        return new BoardService(boardRepository, boardColumnRepository, cardRepository);
    }

    private Board buildBoard(UUID id, String title) {
        Board board = new Board(title);
        ReflectionTestUtils.setField(board, "id", id);
        return board;
    }

    private BoardColumn buildColumn(Board board, UUID id, String title, int orderIndex, List<Card> cards) {
        BoardColumn column = new BoardColumn(board, title, orderIndex);
        ReflectionTestUtils.setField(column, "id", id);
        ReflectionTestUtils.setField(column, "cards", cards);
        return column;
    }

    private Card buildCard(UUID id, String title, int orderIndex) {
        Card card = new Card(null, title, orderIndex);
        ReflectionTestUtils.setField(card, "id", id);
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
        BoardColumn column = buildColumn(board, UUID.randomUUID(), "未着手", 0, List.of(card));

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

    @Test
    void createBoard_savesBoardAndThreeFixedColumns() {
        boardService = service();
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BoardSummaryResponse result = boardService.createBoard(new CreateBoardRequest("新しいボード"));

        assertThat(result.title()).isEqualTo("新しいボード");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BoardColumn>> columnsCaptor = ArgumentCaptor.forClass(List.class);
        verify(boardColumnRepository).saveAll(columnsCaptor.capture());
        List<BoardColumn> savedColumns = columnsCaptor.getValue();

        assertThat(savedColumns).hasSize(3);
        assertThat(savedColumns.get(0).getTitle()).isEqualTo("未着手");
        assertThat(savedColumns.get(0).getOrderIndex()).isZero();
        assertThat(savedColumns.get(1).getTitle()).isEqualTo("進行中");
        assertThat(savedColumns.get(1).getOrderIndex()).isEqualTo(1);
        assertThat(savedColumns.get(2).getTitle()).isEqualTo("完了");
        assertThat(savedColumns.get(2).getOrderIndex()).isEqualTo(2);
    }

    @Test
    void createCard_appendsAtEndOfColumn() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());

        when(boardColumnRepository.findById(columnId)).thenReturn(Optional.of(column));
        when(cardRepository.findMaxOrderIndexByColumnId(columnId)).thenReturn(1);

        CardResponse result = boardService.createCard(boardId, columnId, new CreateCardRequest("新しいタスク"));

        assertThat(result.title()).isEqualTo("新しいタスク");
        assertThat(result.orderIndex()).isEqualTo(2);
    }

    @Test
    void createCard_throwsWhenColumnNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        when(boardColumnRepository.findById(columnId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.createCard(boardId, columnId, new CreateCardRequest("新しいタスク")))
                .isInstanceOf(ColumnNotFoundException.class);
    }

    @Test
    void createCard_throwsWhenColumnBelongsToDifferentBoard() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Board otherBoard = buildBoard(UUID.randomUUID(), "別のボード");
        BoardColumn column = buildColumn(otherBoard, columnId, "未着手", 0, List.of());
        when(boardColumnRepository.findById(columnId)).thenReturn(Optional.of(column));

        assertThatThrownBy(() -> boardService.createCard(boardId, columnId, new CreateCardRequest("新しいタスク")))
                .isInstanceOf(ColumnNotFoundException.class);
    }
}
