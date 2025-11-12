package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StatusUpdateDto {

    @JsonProperty("notification_id")
    private String notificationId;

    private String status;
    private String error;
}
