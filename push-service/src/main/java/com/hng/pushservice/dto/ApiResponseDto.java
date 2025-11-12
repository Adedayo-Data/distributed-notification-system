package com.hng.pushservice.dto;


import lombok.Data;

@Data
public class ApiResponseDto<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Object meta;


    public ApiResponseDto(boolean b, String templateRenderedSuccessfully, T renderedData, Object o) {
    }
}