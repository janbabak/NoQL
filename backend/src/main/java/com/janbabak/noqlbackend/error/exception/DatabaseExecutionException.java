package com.janbabak.noqlbackend.error.exception;

public class DatabaseExecutionException extends Exception {

    @SuppressWarnings("unused")
    public DatabaseExecutionException() {
        super("Query execution failed");
    }

    public DatabaseExecutionException(String message) {
        super(message);
    }
}
