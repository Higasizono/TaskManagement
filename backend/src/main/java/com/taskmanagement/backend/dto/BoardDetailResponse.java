package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.entity.Board;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BoardDetailResponse(
        UUID id,
        String title,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        List<ColumnResponse> columns
) {

    public static BoardDetailResponse from(Board board, List<ColumnResponse> columns) {
        return new BoardDetailResponse(
                board.getId(),
                board.getTitle(),
                board.getCreatedAt(),
                board.getUpdatedAt(),
                columns
        );
    }
}
