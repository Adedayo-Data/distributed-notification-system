package com.hng.templateservice.dto;


import lombok.Data;

@Data
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Object meta; // Using Object to hold PaginationMeta or null

    // --- Constructor for SUCCESS ---
//    public ApiResponseDTO(boolean success, String message, T data, Object meta) {
//        this.success = success;
//        this.message = message;
//        this.data = data;
//        this.meta = meta;
//    }

    // --- Constructor for FAILURE ---
//    public ApiResponseDTO(boolean success, String message, String error, Object meta) {
//        this.success = success;
//        this.message = message;
//        this.error = error;
//        this.meta = meta;
//    }

    public ApiResponseDto(boolean b, String templateRenderedSuccessfully, T renderedData, Object o) {
    }
}