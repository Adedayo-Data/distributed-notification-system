package com.hng.pushservice.dto;

import lombok.Data;

import java.util.Map;

@Data
public class RenderRequestDto {

    private NotificationType notificationType;
    private String templateKey;
    private Map<String, Object> variables;
}
