package com.hng.templateservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hng.templateservice.models.NotificationType;
import lombok.Data;

import java.util.Map;

@Data
public class RenderRequestDto {

    @JsonProperty("notification_type")
    private String notificationType;

    @JsonProperty("template_code")
    private String templateKey;
    private Map<String, String> variables;
}
