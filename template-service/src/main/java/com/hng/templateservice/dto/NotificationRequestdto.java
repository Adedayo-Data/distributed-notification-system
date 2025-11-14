package com.hng.templateservice.dto;

import com.hng.templateservice.models.NotificationType;
import lombok.Data;

@Data
public class NotificationRequestdto {

    private String templateKey;
    private String subjectTemplate;
    private String bodyTemplate;
    private String imageUrl;
    private String actionLink;
    private String type;
    private Integer version;
}
