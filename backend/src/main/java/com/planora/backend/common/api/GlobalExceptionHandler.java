package com.planora.backend.common.api;

import com.planora.backend.common.exception.InvalidAccountReferenceException;
import com.planora.backend.common.exception.OptimisticLockException;
import com.planora.backend.common.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleResourceNotFound(
        ResourceNotFoundException exception
    ) {
        return new ErrorResponse(
            "RESOURCE_NOT_FOUND",
            exception.getMessage(),
            List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(
        MethodArgumentNotValidException exception
    ) {
        List<ValidationError> errors =
            exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                    new ValidationError(
                        error.getField(),
                        error.getDefaultMessage()
                    )
                )
                .toList();

        return new ErrorResponse(
            "VALIDATION_ERROR",
            "Request validation failed",
            errors
        );
    }

    @ExceptionHandler(OptimisticLockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleOptimisticLock(
        OptimisticLockException exception
    ) {
        return new ErrorResponse(
            "OPTIMISTIC_LOCK",
            exception.getMessage(),
            List.of()
        );
    }

    @ExceptionHandler(InvalidAccountReferenceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidAccountReference(
        InvalidAccountReferenceException exception
    ) {
        return new ErrorResponse(
            "INVALID_ACCOUNT_REFERENCE",
            exception.getMessage(),
            List.of()
        );
    }

}
