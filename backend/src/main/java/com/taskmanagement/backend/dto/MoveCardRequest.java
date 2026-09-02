package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveCardRequest(
        @NotNull(message = "移動先の列を指定してください") UUID targetColumnId,
        @Min(value = 0, message = "移動先の位置が不正です") int targetIndex) {}
