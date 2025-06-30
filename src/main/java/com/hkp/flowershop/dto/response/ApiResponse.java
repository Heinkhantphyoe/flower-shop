package com.hkp.flowershop.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generic API Response wrapper for all endpoints
 * Provides consistent response structure across the application
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * Indicates if the operation was successful
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * Human-readable message describing the result
     */
    @JsonProperty("message")
    private String message;

    /**
     * The actual response data (can be any type)
     */
    @JsonProperty("data")
    private T data;

    /**
     * HTTP status code
     */
    @JsonProperty("status")
    private Integer status;

    /**
     * Validation errors (for bad requests)
     */
    @JsonProperty("errors")
    private Map<String, String> errors;



    /**
     * Create a successful response with data and custom message
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(200)
                .build();
    }

    /**
     * Create a successful response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .status(200)
                .build();
    }

    /**
     * Create a successful response with custom message
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(200)
                .build();
    }


    /**
     * Create a created response with custom message
     */
    public static <T> ApiResponse<T> created(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .status(201)
                .build();
    }

    /**
     * Create a created response with no data
     */
    public static <T> ApiResponse<T> created(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(201)
                .build();
    }



    /**
     * Create an error response with status code
     */
    public static ApiResponse<Void> error(String message, Integer status) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(status)
                .build();
    }

    /**
     * Create a bad request response
     */
    public static ApiResponse<Void> badRequest(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(400)
                .build();
    }

    /**
     * Create a validation error response
     */
    public static ApiResponse<Void> validationError(String message, Map<String, String> errors) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(400)
                .errors(errors)
                .build();
    }

    /**
     * Create an unauthorized response
     */
    public static ApiResponse<Void> unauthorized(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(401)
                .build();
    }

    /**
     * Create a forbidden response
     */
    public static ApiResponse<Void> forbidden(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(403)
                .build();
    }

    /**
     * Create a not found response
     */
    public static ApiResponse<Void> notFound(String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .message(message)
                .status(404)
                .build();
    }

}
