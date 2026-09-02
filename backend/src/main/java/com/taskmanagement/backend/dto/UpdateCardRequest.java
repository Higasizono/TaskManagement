package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCardRequest(
        @NotBlank(message = "カード名を入力してください") @Size(max = 100, message = "カード名は100文字以内で入力してください")
                String title) {}
