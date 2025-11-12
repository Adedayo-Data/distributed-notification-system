package com.hng.templateservice.dto;

import com.hng.templateservice.models.NotificationType;
import lombok.Data;

import java.util.Map;

@Data
public class RenderRequestDto {

    private NotificationType notificationType;
    private String templateKey;
    private Map<String, String> variables;
}
