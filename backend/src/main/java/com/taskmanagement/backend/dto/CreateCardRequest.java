package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCardRequest(
        @NotBlank(message = "カード名は必須です") @Size(max = 100, message = "カード名は100文字以内で入力してください")
                String title) {}
