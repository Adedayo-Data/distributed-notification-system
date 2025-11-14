package com.hng.templateservice.dto;


import lombok.Data;

@Data
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Object meta; // Using Object to hold PaginationMeta or null

    // Private constructor so it can only be built using the static methods
    private ApiResponseDto(boolean success, String message, T data, String error, Object meta) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
        this.meta = meta;
    }
  /**
     * Creates a standard SUCCESS response.
     */
    public static <T> ApiResponseDto<T> success(String message, T data, Object meta) {
        // success=true, error=null
        return new ApiResponseDto<T>(true, message, data, null, meta);
    }

    /**
     * Creates a standard FAILURE response.
     */
    public static <T> ApiResponseDto<T> fail(String message, String error, Object meta) {
        // success=false, data=null
        return new ApiResponseDto<T>(false, message, null, error, meta);
    }
}