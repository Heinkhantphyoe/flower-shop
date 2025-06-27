package com.hkp.flowershop.service.util;


import com.hkp.flowershop.dto.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

//import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for creating standardized API responses
 */
@Component
public class ResponseUtil {

    /**
     * Create a successful response with custom message
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success( message));
    }

    /**
     * Create a successful response with data and custom message
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Create a successful response for created resources with custom message
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(data, message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(message));
    }




    /**
     * Create an unauthorized error response
     */
    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.unauthorized(message));
    }



    /**
     * Create a not found error response
     */
    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.notFound(message));
    }


    /**
     * Create an internal server error response
     */
    public static ResponseEntity<ApiResponse<Void>> internalError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(message, 500));
    }

    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.badRequest(message));
    }



    /**
     * Create a validation error response with field errors
     */
    public static ResponseEntity<ApiResponse<Void>> validationError(Map<String, String> fieldErrors) {
        return ResponseEntity.badRequest().body(
                ApiResponse.validationError("Validation failed", fieldErrors)
        );
    }

}