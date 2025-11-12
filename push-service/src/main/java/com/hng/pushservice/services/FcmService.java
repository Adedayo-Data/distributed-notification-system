// File: src/main/java/com/hng/pushservice/services/FcmService.java

package com.hng.pushservice.services;

import com.google.firebase.messaging.*;
import com.hng.pushservice.dto.PushRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FcmService {

    private static final Logger logger = LoggerFactory.getLogger(FcmService.class);

    public String sendPushNotification(PushRequest request) {

        Notification.Builder notificationBuilder = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody());

        if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
            notificationBuilder.setImage(request.getImageUrl());
        }

        Message.Builder messageBuilder = Message.builder()
                .setToken(request.getDeviceToken())
                .setNotification(notificationBuilder.build());

        if (request.getActionLink() != null && !request.getActionLink().isEmpty()) {
            WebpushConfig webpushConfig = WebpushConfig.builder()
                    .setFcmOptions(WebpushFcmOptions.builder()
                            .setLink(request.getActionLink())
                            .build())
                    .build();
            messageBuilder.setWebpushConfig(webpushConfig);
        }

        Message message = messageBuilder.build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            logger.info("Successfully sent message to token {}: {}", request.getDeviceToken(), response);
            return response;
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send push notification to token {}: {}", request.getDeviceToken(), e.getMessage());
            throw new RuntimeException("FCM sending failed", e);
        }
    }
}