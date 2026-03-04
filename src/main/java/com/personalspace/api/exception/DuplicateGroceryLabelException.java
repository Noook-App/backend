package com.personalspace.api.exception;

public class DuplicateGroceryLabelException extends RuntimeException {
    public DuplicateGroceryLabelException(String message) {
        super(message);
    }
}
