package com.hng.pushservice.services;

import com.hng.pushservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class PushMessageServiceReceiver {

    private static final Logger logger = LoggerFactory.getLogger(PushMessageServiceReceiver.class);

    @Value("${app.user-service.url:http://localhost:3001/api/v1/users/}")
    private String userServiceUrl;

    @Value("${app.template-service.url:http://localhost:8085/api/v1/templates/render}")
    private String templateServiceUrl;

    @Value("${app.status-update.url:http://localhost:8000/api/v1/push/status/}")
    private String statusUpdateUrl;

    private final FcmService fcmService;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    @RabbitListener(queues = "push.queue")
    public void consumePushQueue(JobRequestDto jobRequestDto) {
        String notificationId = jobRequestDto.getNotificationId();
        logger.info("Received job for notificationId: {}", notificationId);

        String statusKey = "status:" + notificationId;

        try {
            // Check for duplicate processing
            if (isDuplicateJob(statusKey, notificationId)) {
                logger.info("Conditoin for duplicateJob passed. Skipping.");
                return;
            }

            // Fetch and validate user
            UserResponseDto user = fetchUser(jobRequestDto.getUserId());
            if (!isUserEligibleForPush(user, notificationId, statusKey)) {
                return;
            }

            // Render template
            RenderResponseDto renderedData = renderTemplate(jobRequestDto);

            // Send push notification
            sendPushNotification(user.getPushToken(), renderedData, notificationId);

            // Mark as successful
            logger.info("Successfully processed notificationId: {}", notificationId);
            reportStatus(notificationId, "delivered", null);
            redisTemplate.opsForValue().set(statusKey, "DELIVERED");

        } catch (Exception e) {
            logger.error("Failed to process notificationId {}: {}", notificationId, e.getMessage(), e);
            reportStatus(notificationId, "failed", e.getMessage());
            redisTemplate.opsForValue().set(statusKey, "FAILED");
            throw new RuntimeException("Failing message processing to trigger retry", e);
        }
    }

    private boolean isDuplicateJob(String statusKey, String notificationId) {
        String currentStatus = redisTemplate.opsForValue().get(statusKey);
        if ("DELIVERED".equalsIgnoreCase(currentStatus) || "SKIPPED".equalsIgnoreCase(currentStatus)) {
            logger.warn("Duplicate or completed job {}. Skipping.", notificationId);
            return true;
        }
        return false;
    }

    private UserResponseDto fetchUser(String userId) {
        logger.debug("Fetching user data for userId: {}", userId);
        logger.info("Fetching user data for userId: {}", userId);

        ParameterizedTypeReference<ApiResponseDto<UserResponseDto>> responseType =
                new ParameterizedTypeReference<>() {};

        ResponseEntity<ApiResponseDto<UserResponseDto>> responseEntity = restTemplate.exchange(
                userServiceUrl + userId,
                HttpMethod.GET,
                null,
                responseType
        );

        ApiResponseDto<UserResponseDto> apiResponse = responseEntity.getBody();

        if (apiResponse == null || !apiResponse.isSuccess() || apiResponse.getData() == null) {
            logger.warn("Failed to fetch user data or user not found");
            throw new RuntimeException("Failed to fetch user data or user not found");
        }

        UserResponseDto user = apiResponse.getData();
        logger.debug("Fetched user: {} with preferences: {}", user.getId(), user.getPreferences());
        logger.info("Fetched user: {} with preferences: {}", user.getId(), user.getPreferences());
        
        return user;
    }

    private boolean isUserEligibleForPush(UserResponseDto user, String notificationId, String statusKey) {
        if (user.getPreferences() == null || !user.getPreferences().getPush()) {
            logger.warn("User {} has push notifications disabled. Skipping.", user.getId());
            reportStatus(notificationId, "failed", "User has push notifications disabled");
            redisTemplate.opsForValue().set(statusKey, "SKIPPED");
            return false;
        }

        String pushToken = user.getPushToken();
        if (pushToken == null || pushToken.isEmpty()) {
            logger.warn("User {} has no push token. Skipping.", user.getId());
            reportStatus(notificationId, "failed", "No push token available");
            redisTemplate.opsForValue().set(statusKey, "SKIPPED");
            return false;
        }

        return true;
    }

    private RenderResponseDto renderTemplate(JobRequestDto jobRequestDto) {
        logger.debug("Rendering template: {}", jobRequestDto.getTemplateCode());

        RenderRequestDto renderRequest = new RenderRequestDto();
        renderRequest.setTemplateKey(jobRequestDto.getTemplateCode());
        renderRequest.setNotificationType("push");
        renderRequest.setVariables(jobRequestDto.getVariables());

        HttpEntity<RenderRequestDto> requestEntity = new HttpEntity<>(renderRequest);
        ParameterizedTypeReference<ApiResponseDto<RenderResponseDto>> responseType =
                new ParameterizedTypeReference<ApiResponseDto<RenderResponseDto>>() {};

        ResponseEntity<ApiResponseDto<RenderResponseDto>> responseEntity = restTemplate.exchange(
                templateServiceUrl,
                HttpMethod.POST,
                requestEntity,
                responseType
        );

        ApiResponseDto<RenderResponseDto> templateResponse = responseEntity.getBody();

        if (templateResponse == null || !templateResponse.isSuccess()) {
            throw new RuntimeException("Failed to render template: " +
                    (templateResponse != null ? templateResponse.getError() : "null response"));
        }

        RenderResponseDto renderedData = templateResponse.getData();
        if (renderedData == null) {
            throw new RuntimeException("Rendered data came back empty.");
        }

        return renderedData;
    }

    private void sendPushNotification(String pushToken, RenderResponseDto renderedData, String notificationId) {
        logger.debug("Sending push notification to token: {}", pushToken);

        PushRequest fcmRequest = new PushRequest();
        fcmRequest.setDeviceToken(pushToken);
        fcmRequest.setTitle(renderedData.getRenderedSubject());
        fcmRequest.setBody(renderedData.getRenderedBody());
        fcmRequest.setNotificationId(notificationId);
        fcmRequest.setImageUrl(renderedData.getRenderedImageUrl());
        fcmRequest.setActionLink(renderedData.getRenderedActionLink());

        fcmService.sendPushNotification(fcmRequest);
    }

    private void reportStatus(String notificationId, String status, String error) {
        try {
            StatusUpdateDto statusUpdate = new StatusUpdateDto();
            statusUpdate.setNotificationId(notificationId);
            statusUpdate.setStatus(status);
            statusUpdate.setError(error);

            logger.debug("Reporting status to Gateway: {}", statusUpdate);
            restTemplate.postForObject(statusUpdateUrl, statusUpdate, Object.class);

        } catch (HttpClientErrorException.NotFound e) {
            logger.warn("Notification {} not found in gateway (OK for direct RabbitMQ testing)", notificationId);
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to report status for notificationId {}: {}",
                    notificationId, e.getMessage());
        }
    }
}