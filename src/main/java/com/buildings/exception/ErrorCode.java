package com.buildings.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_TOKEN(1008, "Token is invalid", HttpStatus.UNAUTHORIZED),
    RESOURCE_NOT_FOUND(1009, "Resource not found", HttpStatus.NOT_FOUND),
    INVALID_UUID(1010, "Invalid UUID format", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_PASSWORD(1010, "Invalid email or password", HttpStatus.BAD_REQUEST),
    BUILDING_CODE_ARE_EXIST(1011, "Building code are exist", HttpStatus.NOT_FOUND),
    BUILDING_NAME_ARE_EXIST(1011, "Building name are exist", HttpStatus.NOT_FOUND),
    BUILDING_NOT_FOUND(1012, "Building not found", HttpStatus.NOT_FOUND),
    CANNOT_MODIFY_LAYOUT(1013, "Can't modify layout ", HttpStatus.BAD_REQUEST),
    BUILD_HAS_APARTMENT(1014, "Build has apartment", HttpStatus.BAD_REQUEST),
    APARTMENT_ALREADY_GENERATED(1015, "Apartment already generated", HttpStatus.BAD_REQUEST),
    APARTMENT_NOT_FOUND(1016, "Apartment not found", HttpStatus.NOT_FOUND),
    // Service & Tariff errors (2xxx)
    SERVICE_NOT_FOUND(2001, "Service not found", HttpStatus.NOT_FOUND),
    SERVICE_CODE_EXISTED(2002, "Service code already exists", HttpStatus.BAD_REQUEST),
    TARIFF_NOT_FOUND(2003, "Tariff not found", HttpStatus.NOT_FOUND),
    TARIFF_OVERLAPPING(2004, "Tariff period overlaps with existing tariff", HttpStatus.BAD_REQUEST),
    SERVICE_HAS_ACTIVE_READINGS(2005, "Cannot deactivate service: it has active meter readings in current period",
            HttpStatus.BAD_REQUEST),
    SERVICE_ALREADY_INACTIVE(2006, "Service is already inactive", HttpStatus.BAD_REQUEST),
    SERVICE_ALREADY_ACTIVE(2007, "Service is already active", HttpStatus.BAD_REQUEST),

    // Meter Reading errors (3xxx)
    METER_READING_NOT_FOUND(3001, "Meter reading not found", HttpStatus.NOT_FOUND),
    METER_READING_LOCKED(3002, "Meter reading is locked and cannot be modified", HttpStatus.BAD_REQUEST),
    INVALID_METER_INDEX(3003, "New index must be greater than or equal to old index", HttpStatus.BAD_REQUEST),
    DUPLICATE_METER_READING(3004, "Meter reading already exists for this apartment, service and period",
            HttpStatus.BAD_REQUEST),
    // File errors (5xxx)
    FILE_UPLOAD_FAILED(5001, "Failed to upload file", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_NOT_FOUND(5002, "File not found", HttpStatus.NOT_FOUND),
    FILE_TYPE_NOT_ALLOWED(5003, "File type not allowed", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(5004, "File size exceeded the limit", HttpStatus.BAD_REQUEST),
    ;
    ;

    ErrorCode(int code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatus statusCode;
}