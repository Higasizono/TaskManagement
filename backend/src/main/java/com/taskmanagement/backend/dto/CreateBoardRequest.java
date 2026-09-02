package com.taskmanagement.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBoardRequest(
        @NotBlank(message = "ボード名は必須です") @Size(max = 50, message = "ボード名は50文字以内で入力してください")
                String title) {}
