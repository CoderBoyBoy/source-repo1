package com.gitmanager.exception;

import com.gitmanager.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RepositoryException.class)
    public ResponseEntity<ApiResponse<Void>> handleRepositoryException(RepositoryException ex) {
        logger.error("Repository error: {}", ex.getMessage(), ex);
        
        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
        return ResponseEntity.status(status)
                .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ApiResponse<Map<String, String>> response = new ApiResponse<>(false, "Validation failed", errors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        logger.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }

    private HttpStatus mapErrorCodeToStatus(RepositoryException.ErrorCode errorCode) {
        return switch (errorCode) {
            case REPOSITORY_NOT_FOUND, BRANCH_NOT_FOUND, TAG_NOT_FOUND, FILE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case REPOSITORY_ALREADY_EXISTS, BRANCH_ALREADY_EXISTS, TAG_ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case INVALID_OPERATION, MERGE_CONFLICT -> HttpStatus.BAD_REQUEST;
            case SSH_ERROR, CLONE_FAILED -> HttpStatus.BAD_GATEWAY;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
