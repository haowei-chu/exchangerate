package org.example.exception;

public class ApiException extends RuntimeException {

    private final ApiErrorCode errorCode;

    public ApiException(ApiErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public String getMessage() {
        return errorCode.getDefaultMessage();
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return errorCode.getHttpStatus().value();
    }
}