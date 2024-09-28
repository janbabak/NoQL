package com.janbabak.noqlbackend.error.handler;

import com.janbabak.noqlbackend.error.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * This class handles exceptions thrown during REST API requests.<br />
 * Set status code accordingly.
 */
@RestControllerAdvice
public class RestResponseEntityExceptionHandler {

    /**
     * Entity not found - 404 <br />
     * The issue is on users side - requested not existing entity...
     *
     * @param e exception
     * @return exception message
     */
    @ExceptionHandler({EntityNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFoundException(Exception e) {
        return e.getMessage();
    }

    /**
     * Bad request - 400 <br />
     * The issue is on the user's side - database not available, ...
     *
     * @param e exception
     * @return error message
     */
    @ExceptionHandler({DatabaseConnectionException.class, UserAlreadyExistsException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(Exception e) {
        return e.getMessage();
    }

    /**
     * Bad request - 400 <br />
     * Input is not valid.
     *
     * @param e exception
     * @return map of errors - filed->error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

    /**
     * Internal server error - 500 <br />
     * The issue is on our side (SQL syntax error, ...) or LLM model side.
     *
     * @param e exception
     * @return error message
     */
    @ExceptionHandler({LLMException.class, DatabaseExecutionException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleInternalServerErrorExceptions(Exception e) {
        return e.getMessage();
    }
}
