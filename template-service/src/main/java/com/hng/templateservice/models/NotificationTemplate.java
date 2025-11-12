package com.hng.templateservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonProperty("template_code")
    @Column(unique = true)
    private String templateKey;

    private String subjectTemplate;
    private String bodyTemplate;

    private NotificationType type;
    private Integer version;
}




