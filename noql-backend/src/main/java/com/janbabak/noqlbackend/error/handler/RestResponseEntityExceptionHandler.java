package com.janbabak.noqlbackend.error.handler;

import com.janbabak.noqlbackend.error.exception.DatabaseConnectionException;
import com.janbabak.noqlbackend.error.exception.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This class handles exceptions thrown during REST API requests.<br />
 * Set status code accordingly.
 */
@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    /**
     * Entity not found - 404
     *
     * @param e exception
     * @return exception message
     */
    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String entityNotFoundException(Exception e) {
        return e.getMessage();
    }

    /**
     * Bad request - 400
     *
     * @param e exception
     * @return error message
     */
    @ExceptionHandler({DatabaseConnectionException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badRequestException(Exception e) {
        return e.getMessage();
    }
}
