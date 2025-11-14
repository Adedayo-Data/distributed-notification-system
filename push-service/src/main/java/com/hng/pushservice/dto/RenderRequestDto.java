package com.hng.pushservice.dto;

import lombok.Data;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RenderRequestDto {

    @JsonProperty("notification_type")
    private String notificationType;

    @JsonProperty("template_code")
    private String templateKey;
    
    private Map<String, String> variables;
}
