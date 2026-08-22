package com.taskmanagement.backend.controller;

import com.taskmanagement.backend.dto.BoardSummaryResponse;
import com.taskmanagement.backend.exception.BoardNotFoundException;
import com.taskmanagement.backend.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

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

        mockMvc.perform(get("/api/boards/{boardId}", boardId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBoard_returns201AndBody() throws Exception {
        UUID boardId = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();
        when(boardService.createBoard(any()))
                .thenReturn(new BoardSummaryResponse(boardId, "新しいボード", now, now));

        mockMvc.perform(post("/api/boards")
                        .contentType("application/json")
                        .content("{\"title\":\"新しいボード\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(boardId.toString()))
                .andExpect(jsonPath("$.title").value("新しいボード"));
    }

    @Test
    void createBoard_returns400WhenTitleBlank() throws Exception {
        mockMvc.perform(post("/api/boards")
                        .contentType("application/json")
                        .content("{\"title\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
