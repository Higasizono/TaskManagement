package com.taskmanagement.backend.service;

import com.taskmanagement.backend.dto.BoardDetailResponse;
import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
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
import static org.mockito.Mockito.never;
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

    private Card buildCard(UUID id, BoardColumn column, String title, int orderIndex) {
        Card card = new Card(column, title, orderIndex);
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
    void deleteBoard_deletesBoardViaRepository() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        when(boardRepository.existsById(boardId)).thenReturn(true);

        boardService.deleteBoard(boardId);

        verify(boardRepository).deleteById(boardId);
    }

    @Test
    void deleteBoard_throwsWhenBoardNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        when(boardRepository.existsById(boardId)).thenReturn(false);

        assertThatThrownBy(() -> boardService.deleteBoard(boardId))
                .isInstanceOf(BoardNotFoundException.class);

        verify(boardRepository, never()).deleteById(any());
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

    @Test
    void updateCardTitle_renamesCardAndReturnsResponse() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        Card card = buildCard(cardId, column, "元のタイトル", 0);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        CardResponse result = boardService.updateCardTitle(boardId, columnId, cardId, new UpdateCardRequest("新しいタイトル"));

        assertThat(result.title()).isEqualTo("新しいタイトル");
    }

    @Test
    void updateCardTitle_throwsWhenCardNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.updateCardTitle(boardId, columnId, cardId, new UpdateCardRequest("新しいタイトル")))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void updateCardTitle_throwsWhenCardBelongsToDifferentColumn() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID otherColumnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn otherColumn = buildColumn(board, otherColumnId, "未着手", 0, List.of());
        Card card = buildCard(cardId, otherColumn, "タイトル", 0);
        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> boardService.updateCardTitle(boardId, columnId, cardId, new UpdateCardRequest("新しいタイトル")))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void moveCard_reordersWithinSameColumn() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        Card cardA = buildCard(UUID.randomUUID(), column, "A", 0);
        Card cardB = buildCard(UUID.randomUUID(), column, "B", 1);
        Card cardC = buildCard(UUID.randomUUID(), column, "C", 2);

        when(cardRepository.findById(cardB.getId())).thenReturn(Optional.of(cardB));
        when(cardRepository.findByColumnIdOrderByOrderIndexAsc(columnId))
                .thenReturn(List.of(cardA, cardB, cardC));

        boardService.moveCard(boardId, columnId, cardB.getId(), new MoveCardRequest(columnId, 0));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Card>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(cardRepository).saveAll(savedCaptor.capture());
        List<Card> saved = savedCaptor.getValue();

        assertThat(saved).extracting(Card::getId).containsExactly(cardB.getId(), cardA.getId(), cardC.getId());
        assertThat(cardB.getOrderIndex()).isZero();
        assertThat(cardA.getOrderIndex()).isEqualTo(1);
        assertThat(cardC.getOrderIndex()).isEqualTo(2);
    }

    @Test
    void moveCard_onlyTouchesCardsWhoseIndexActuallyChanged() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        Card cardA = buildCard(UUID.randomUUID(), column, "A", 0);
        Card cardB = buildCard(UUID.randomUUID(), column, "B", 1);
        Card cardC = buildCard(UUID.randomUUID(), column, "C", 2);

        when(cardRepository.findById(cardC.getId())).thenReturn(Optional.of(cardC));
        when(cardRepository.findByColumnIdOrderByOrderIndexAsc(columnId))
                .thenReturn(List.of(cardA, cardB, cardC));

        boardService.moveCard(boardId, columnId, cardC.getId(), new MoveCardRequest(columnId, 1));

        assertThat(cardA.getOrderIndex()).isZero();
        assertThat(cardA.getUpdatedAt()).isEqualTo(cardA.getCreatedAt());
        assertThat(cardC.getOrderIndex()).isEqualTo(1);
        assertThat(cardB.getOrderIndex()).isEqualTo(2);
    }

    @Test
    void moveCard_movesCardToDifferentColumnAndReindexesBothColumns() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID sourceColumnId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn sourceColumn = buildColumn(board, sourceColumnId, "未着手", 0, List.of());
        BoardColumn targetColumn = buildColumn(board, targetColumnId, "進行中", 1, List.of());
        Card cardA0 = buildCard(UUID.randomUUID(), sourceColumn, "A0", 0);
        Card cardA1 = buildCard(UUID.randomUUID(), sourceColumn, "A1", 1);
        Card cardB0 = buildCard(UUID.randomUUID(), targetColumn, "B0", 0);

        when(cardRepository.findById(cardA1.getId())).thenReturn(Optional.of(cardA1));
        when(boardColumnRepository.findById(targetColumnId)).thenReturn(Optional.of(targetColumn));
        when(cardRepository.findByColumnIdOrderByOrderIndexAsc(sourceColumnId))
                .thenReturn(List.of(cardA0, cardA1));
        when(cardRepository.findByColumnIdOrderByOrderIndexAsc(targetColumnId))
                .thenReturn(List.of(cardB0));

        CardResponse result = boardService.moveCard(boardId, sourceColumnId, cardA1.getId(),
                new MoveCardRequest(targetColumnId, 0));

        assertThat(result.title()).isEqualTo("A1");
        assertThat(cardA1.getColumn()).isEqualTo(targetColumn);
        assertThat(cardA1.getOrderIndex()).isZero();
        assertThat(cardA0.getOrderIndex()).isZero();
        assertThat(cardB0.getOrderIndex()).isEqualTo(1);
    }

    @Test
    void moveCard_throwsWhenCardNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.moveCard(boardId, columnId, cardId, new MoveCardRequest(columnId, 0)))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    void moveCard_throwsWhenTargetColumnNotFound() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        Card card = buildCard(UUID.randomUUID(), column, "タスク", 0);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(boardColumnRepository.findById(targetColumnId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> boardService.moveCard(boardId, columnId, card.getId(),
                new MoveCardRequest(targetColumnId, 0)))
                .isInstanceOf(ColumnNotFoundException.class);
    }

    @Test
    void moveCard_throwsWhenTargetColumnBelongsToDifferentBoard() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        Board otherBoard = buildBoard(UUID.randomUUID(), "別のボード");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        BoardColumn targetColumn = buildColumn(otherBoard, targetColumnId, "進行中", 1, List.of());
        Card card = buildCard(UUID.randomUUID(), column, "タスク", 0);
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(boardColumnRepository.findById(targetColumnId)).thenReturn(Optional.of(targetColumn));

        assertThatThrownBy(() -> boardService.moveCard(boardId, columnId, card.getId(),
                new MoveCardRequest(targetColumnId, 0)))
                .isInstanceOf(ColumnNotFoundException.class);
    }

    @Test
    void moveCard_clampsTargetIndexWhenGreaterThanColumnSize() {
        boardService = service();
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        Board board = buildBoard(boardId, "個人開発");
        BoardColumn column = buildColumn(board, columnId, "未着手", 0, List.of());
        Card cardA = buildCard(UUID.randomUUID(), column, "A", 0);
        Card cardB = buildCard(UUID.randomUUID(), column, "B", 1);

        when(cardRepository.findById(cardA.getId())).thenReturn(Optional.of(cardA));
        when(cardRepository.findByColumnIdOrderByOrderIndexAsc(columnId))
                .thenReturn(List.of(cardA, cardB));

        boardService.moveCard(boardId, columnId, cardA.getId(), new MoveCardRequest(columnId, 99));

        assertThat(cardA.getOrderIndex()).isEqualTo(1);
        assertThat(cardB.getOrderIndex()).isZero();
    }
}
