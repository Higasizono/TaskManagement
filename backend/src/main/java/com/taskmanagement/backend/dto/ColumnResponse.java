package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.entity.BoardColumn;

import java.util.List;
import java.util.UUID;

public record ColumnResponse(
        UUID id,
        String title,
        int orderIndex,
        List<CardResponse> cards
) {

    public static ColumnResponse from(BoardColumn column) {
        return new ColumnResponse(
                column.getId(),
                column.getTitle(),
                column.getOrderIndex(),
                column.getCards().stream().map(CardResponse::from).toList()
        );
    }
}
