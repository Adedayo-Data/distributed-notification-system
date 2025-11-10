package com.hng.pushservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PushMessage {

    @JsonProperty("notification_id")
    private String notificationId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("push_token")
    private String pushToken;

    @JsonProperty("notification_type")
    private String notificationType;

    private String priority;

    private Payload payload;

    @Data
    public static class Payload{

        private String title;
        private String body;

        @JsonProperty("image_url")
        private String imageUrl;

        @JsonProperty("action_link")
        private String actionLink;
    }
}
