package com.hng.pushservice.dto;

import lombok.Data;

@Data
public class NotificationRequestdto {

    private String templateKey;
    private String subjectTemplate;
    private String bodyTemplate;
    private NotificationType type;
    private Integer version;
}
