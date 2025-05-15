package org.example.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {

    ERROR_UNSUPPORTED_BASE_CURRENCY("API-1000", "Unsupported base currency", HttpStatus.BAD_REQUEST),
    ERROR_NO_AVAILABLE_PROVIDERS("API-1001", "No providers are available", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;
    private final String defaultMessage;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.defaultMessage = message;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}