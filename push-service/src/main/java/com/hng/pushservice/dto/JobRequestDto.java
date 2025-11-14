package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class JobRequestDto {

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("template_code")
    private String templateCode;

    private Map<String, String> variables;
}
