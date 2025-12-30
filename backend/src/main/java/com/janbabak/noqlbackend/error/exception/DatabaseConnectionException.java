package com.janbabak.noqlbackend.error.exception;

public class DatabaseConnectionException extends Exception {

    public DatabaseConnectionException() {
        super("Connection to database failed.");
    }

    public DatabaseConnectionException(String message) {
        super(message);
    }

    public DatabaseConnectionException(String message, Exception e) {
        super(message, e);
    }
}
