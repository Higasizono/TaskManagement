package com.taskmanagement.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.dto.CardResponse;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.exception.CardNotFoundException;
import com.taskmanagement.backend.exception.ColumnNotFoundException;
import com.taskmanagement.backend.service.BoardService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private BoardService boardService;

    @Test
    void getAllBoards_returnsJsonArray() throws Exception {
        UUID boardId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.getAllBoards())
                .thenReturn(List.of(new BoardSummaryResponse(boardId, "スクール課題", now, now)));

        mockMvc.perform(get("/api/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(boardId.toString()))
                .andExpect(jsonPath("$[0].title").value("スクール課題"));
    }

    @Test
    void getBoardDetail_returns404WhenBoardNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        when(boardService.getBoardDetail(boardId)).thenThrow(new BoardNotFoundException(boardId));

        mockMvc.perform(get("/api/boards/{boardId}", boardId)).andExpect(status().isNotFound());
    }

    @Test
    void createBoard_returns201AndBody() throws Exception {
        UUID boardId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.createBoard(any()))
                .thenReturn(new BoardSummaryResponse(boardId, "新しいボード", now, now));

        mockMvc.perform(
                        post("/api/boards")
                                .contentType("application/json")
                                .content("{\"title\":\"新しいボード\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(boardId.toString()))
                .andExpect(jsonPath("$.title").value("新しいボード"));
    }

    @Test
    void createBoard_returns400WhenTitleBlank() throws Exception {
        mockMvc.perform(
                        post("/api/boards")
                                .contentType("application/json")
                                .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBoard_returns204() throws Exception {
        UUID boardId = UUID.randomUUID();

        mockMvc.perform(delete("/api/boards/{boardId}", boardId)).andExpect(status().isNoContent());
    }

    @Test
    void deleteBoard_returns404WhenBoardNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        doThrow(new BoardNotFoundException(boardId)).when(boardService).deleteBoard(boardId);

        mockMvc.perform(delete("/api/boards/{boardId}", boardId)).andExpect(status().isNotFound());
    }

    @Test
    void createCard_returns201AndBody() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.createCard(eq(boardId), eq(columnId), any()))
                .thenReturn(new CardResponse(cardId, "新しいタスク", 0, now, now));

        mockMvc.perform(
                        post("/api/boards/{boardId}/columns/{columnId}/cards", boardId, columnId)
                                .contentType("application/json")
                                .content("{\"title\":\"新しいタスク\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.title").value("新しいタスク"));
    }

    @Test
    void createCard_returns404WhenColumnNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        when(boardService.createCard(eq(boardId), eq(columnId), any()))
                .thenThrow(new ColumnNotFoundException(columnId));

        mockMvc.perform(
                        post("/api/boards/{boardId}/columns/{columnId}/cards", boardId, columnId)
                                .contentType("application/json")
                                .content("{\"title\":\"新しいタスク\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_returns400WhenTitleBlank() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();

        mockMvc.perform(
                        post("/api/boards/{boardId}/columns/{columnId}/cards", boardId, columnId)
                                .contentType("application/json")
                                .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_returns200AndBody() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.updateCardTitle(eq(boardId), eq(columnId), eq(cardId), any()))
                .thenReturn(new CardResponse(cardId, "更新後のタイトル", 0, now, now));

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"title\":\"更新後のタイトル\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()))
                .andExpect(jsonPath("$.title").value("更新後のタイトル"));
    }

    @Test
    void updateCard_returns404WhenCardNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        when(boardService.updateCardTitle(eq(boardId), eq(columnId), eq(cardId), any()))
                .thenThrow(new CardNotFoundException(cardId));

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"title\":\"更新後のタイトル\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCard_returns400WhenTitleBlank() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCard_returns400WhenTitleExceeds100Chars() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        String tooLongTitle = "あ".repeat(101);

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"title\":\"" + tooLongTitle + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteCard_returns204() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(
                        delete(
                                "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                boardId,
                                columnId,
                                cardId))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCard_returns404WhenCardNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        doThrow(new CardNotFoundException(cardId))
                .when(boardService)
                .deleteCard(boardId, columnId, cardId);

        mockMvc.perform(
                        delete(
                                "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}",
                                boardId,
                                columnId,
                                cardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveCard_returns200AndBody() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.moveCard(eq(boardId), eq(columnId), eq(cardId), any()))
                .thenReturn(new CardResponse(cardId, "タスク", 0, now, now));

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content(
                                        "{\"targetColumnId\":\""
                                                + targetColumnId
                                                + "\",\"targetIndex\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId.toString()));
    }

    @Test
    void moveCard_returns404WhenCardNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        when(boardService.moveCard(eq(boardId), eq(columnId), eq(cardId), any()))
                .thenThrow(new CardNotFoundException(cardId));

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content(
                                        "{\"targetColumnId\":\""
                                                + targetColumnId
                                                + "\",\"targetIndex\":0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveCard_returns404WhenTargetColumnNotFound() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();
        when(boardService.moveCard(eq(boardId), eq(columnId), eq(cardId), any()))
                .thenThrow(new ColumnNotFoundException(targetColumnId));

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content(
                                        "{\"targetColumnId\":\""
                                                + targetColumnId
                                                + "\",\"targetIndex\":0}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void moveCard_returns400WhenTargetColumnIdMissing() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"targetIndex\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveCard_returns400WhenTargetIndexMissing() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content("{\"targetColumnId\":\"" + targetColumnId + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void moveCard_returns400WhenTargetIndexIsNegative() throws Exception {
        UUID boardId = UUID.randomUUID();
        UUID columnId = UUID.randomUUID();
        UUID cardId = UUID.randomUUID();
        UUID targetColumnId = UUID.randomUUID();

        mockMvc.perform(
                        patch(
                                        "/api/boards/{boardId}/columns/{columnId}/cards/{cardId}/move",
                                        boardId,
                                        columnId,
                                        cardId)
                                .contentType("application/json")
                                .content(
                                        "{\"targetColumnId\":\""
                                                + targetColumnId
                                                + "\",\"targetIndex\":-1}"))
                .andExpect(status().isBadRequest());
    }
}
