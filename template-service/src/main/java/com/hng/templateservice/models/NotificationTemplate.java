package com.hng.templateservice.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "notification_template")
@Data
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @JsonProperty("template_code")
    @Column(unique = true)
    private String templateKey;

    @JsonProperty("subject_template")
    private String subjectTemplate;

    @JsonProperty("body_template")
    private String bodyTemplate;

    @JsonProperty("image_url")    
    private String imageUrl;

    @JsonProperty("action_link")
    private String actionLink;
    
    private String type;
    private Integer version;
}




