package com.taskmanagement.backend.exception;

import java.util.UUID;

public class BoardNotFoundException extends RuntimeException {

    public BoardNotFoundException(UUID boardId) {
        super("Board not found: " + boardId);
    }
}
