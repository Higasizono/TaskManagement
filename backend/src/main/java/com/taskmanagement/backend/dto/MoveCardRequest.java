package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveCardRequest(
        @NotNull(message = "移動先の列を指定してください") UUID targetColumnId,
        // primitive にするとJSONから欠落しても 0 として通り @Min が機能しないため、
        // ラッパ型 + @NotNull で「未指定」を検出できるようにしている。
        @NotNull(message = "移動先の位置を指定してください") @Min(value = 0, message = "移動先の位置が不正です")
                Integer targetIndex) {}
