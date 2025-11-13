package com.hng.pushservice.dto;

import lombok.Data;

@Data
public class PushRequest {

    // Required fields
    private String deviceToken;
    private String body;
    private String notificationId;

    // Optional fields
    private String title;
    private String imageUrl;
    private String actionLink;

}
