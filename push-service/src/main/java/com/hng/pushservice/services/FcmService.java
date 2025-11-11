package com.hng.pushservice.services;

import com.google.firebase.messaging.*;
import com.hng.pushservice.dto.PushMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    public String sendPushNotification(PushMessage request) {

        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getPayload().getTitle())
                .setBody(request.getPayload().getBody());

        if (request.getPayload().getImageUrl() != null && !request.getPayload().getImageUrl().isEmpty()) {
            notificationBuilder.setImage(request.getPayload().getImageUrl());
        }

        Message.Builder messageBuilder = Message.builder()
                .setToken(request.getPushToken())
                .setNotification(notificationBuilder.build());

        // Handle the optional actionLink by setting it on the WebpushConfig
        if (request.getPayload().getActionLink() != null && !request.getPayload().getActionLink().isEmpty()) {
            WebpushConfig webpushConfig = WebpushConfig.builder()
                    .setFcmOptions(WebpushFcmOptions.builder()
                            .setLink(request.getPayload().getActionLink())
                            .build())
                    .build();
            messageBuilder.setWebpushConfig(webpushConfig);
        }

        Message message = messageBuilder.build();

        try {
            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent message to token {}: {}", request.getPushToken(), response);
            return response; // Returns the message ID
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push notification to token {}: {}", request.getPushToken(), e.getMessage());
            // In Phase 3, this exception will trigger the retry logic
            throw new RuntimeException("FCM sending failed", e);
        }
    }
}
