package com.devteria.identityservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INTERNAL_SERVER_ERROR(5000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    SELECTOR_NOT_MATCH(1009, "Selector does not match any content on the target page", HttpStatus.BAD_REQUEST),
    SELECTOR_NOT_BELONG_TO_ANY_CRAWL_SOURCE(1010, "Selector does not belong to any crawl source", HttpStatus.BAD_REQUEST),
    DATA_NOT_FOUND(1011, "Data not found", HttpStatus.NOT_FOUND),
    DATA_ALREADY_EXISTED(1012, "Data already existed", HttpStatus.BAD_REQUEST),
    CAN_NOT_RENDER_HTML_WITH_JS(1013, "Can not render HTML with JS", HttpStatus.INTERNAL_SERVER_ERROR),
    CAN_NOT_EXTRACT_ID_FROM_URL(1014, "Can not extract ID from URL", HttpStatus.INTERNAL_SERVER_ERROR),
    DATA_IS_NOT_VALID(1015, "Data is not valid", HttpStatus.BAD_REQUEST),
    DOMAIN_NOT_SUPPORTED(1016, "Domain not supported", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
