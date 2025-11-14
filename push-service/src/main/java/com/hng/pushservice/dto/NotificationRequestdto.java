package com.hng.pushservice.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class NotificationRequestdto {

    @JsonProperty("template_code")
    private String templateKey;
    private String subjectTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private Integer version;
}
