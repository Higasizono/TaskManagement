package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.entity.Board;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BoardSummaryResponse(
        UUID id,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static BoardSummaryResponse from(Board board) {
        return new BoardSummaryResponse(
                board.getId(),
                board.getTitle(),
                board.getCreatedAt(),
                board.getUpdatedAt()
        );
    }
}
