package com.taskmanagement.backend.exception;

import java.util.UUID;

public class ColumnNotFoundException extends RuntimeException {

    public ColumnNotFoundException(UUID columnId) {
        super("Column not found: " + columnId);
    }
}
