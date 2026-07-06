package com.taskmanagement.backend.dto;

import com.taskmanagement.backend.entity.Card;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CardResponse(
        UUID id,
        String title,
        int orderIndex,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static CardResponse from(Card card) {
        return new CardResponse(
                card.getId(),
                card.getTitle(),
                card.getOrderIndex(),
                card.getCreatedAt(),
                card.getUpdatedAt()
        );
    }
}
