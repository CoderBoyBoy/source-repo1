package com.gitmanager.exception;

public class RepositoryException extends RuntimeException {

    private final ErrorCode errorCode;

    public RepositoryException(String message) {
        super(message);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public RepositoryException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = ErrorCode.INTERNAL_ERROR;
    }

    public RepositoryException(String message, Throwable cause, ErrorCode errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        REPOSITORY_NOT_FOUND,
        REPOSITORY_ALREADY_EXISTS,
        BRANCH_NOT_FOUND,
        BRANCH_ALREADY_EXISTS,
        TAG_NOT_FOUND,
        TAG_ALREADY_EXISTS,
        INVALID_OPERATION,
        SSH_ERROR,
        CLONE_FAILED,
        MERGE_CONFLICT,
        FILE_NOT_FOUND,
        INTERNAL_ERROR
    }
}
