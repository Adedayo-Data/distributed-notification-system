package com.hng.pushservice.services;

import com.hng.pushservice.dto.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RabbitListener(queues = "push.queue")
@RequiredArgsConstructor
public class PushMessageServiceReceiver {

    private final Logger logger = LoggerFactory.getLogger(PushMessageServiceReceiver.class);

    private static final String USER_SERVICE_URL = "http://localhost:3001/api/v1/users/";
    private static final String TEMPLATE_SERVICE_URL = "http://localhost:8085/api/v1/templates/render";
    private static final String STATUS_UPDATE_URL = "http://localhost:8000/api/v1/push/status/";

    private final FcmService fcmService;
    private final StringRedisTemplate redisTemplate;
    private final RestTemplate restTemplate;

    @RabbitHandler
    public void consumePushQueue(JobRequestDto jobRequestDto){
        String notificationId = jobRequestDto.getNotificationId();
        logger.info("Received job for notificationId: {}", notificationId);

        String statusKey = "status:" + notificationId;

        try {
            String currentStatus = redisTemplate.opsForValue().get(statusKey);
            if ("DELIVERED".equals(currentStatus) || "SKIPPED".equals(currentStatus)) {
                logger.warn("Duplicate or completed job {}. Skipping.", notificationId);
                return;
            }

            logger.debug("Fetching user data for userId: {}", jobRequestDto.getUserId());

            UserResponseDto user = restTemplate.getForObject(
                    USER_SERVICE_URL + jobRequestDto.getUserId(),
                    UserResponseDto.class
            );

            if (user == null || user.getPreferences() == null || !user.getPreferences().getPush()) {
                logger.warn("User {} has push notifications disabled. Skipping.", jobRequestDto.getUserId());
                reportStatus(notificationId, "SKIPPED_PREFERENCE", null);
                redisTemplate.opsForValue().set(statusKey, "SKIPPED");
                return;
            }

            String pushToken = user.getPushToken();
            if (pushToken == null || pushToken.isEmpty()) {
                logger.warn("User {} has no push token. Skipping.", jobRequestDto.getUserId());
                reportStatus(notificationId, "SKIPPED_NO_TOKEN", null);
                redisTemplate.opsForValue().set(statusKey, "SKIPPED");
                return;
            }

            logger.debug("Rendering template: {}", jobRequestDto.getTemplateCode());
            RenderRequestDto renderRequest = new RenderRequestDto();
            renderRequest.setTemplateKey(jobRequestDto.getTemplateCode());
            renderRequest.setNotificationType(NotificationType.PUSH);
            renderRequest.setVariables(jobRequestDto.getVariables());

            HttpEntity<RenderRequestDto> requestEntity = new HttpEntity<>(renderRequest);
            ParameterizedTypeReference<ApiResponseDto<RenderResponseDto>> responseType =
                    new ParameterizedTypeReference<ApiResponseDto<RenderResponseDto>>() {};

            ResponseEntity<ApiResponseDto<RenderResponseDto>> responseEntity = restTemplate.exchange(
                    TEMPLATE_SERVICE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    responseType
            );

            ApiResponseDto<RenderResponseDto> templateResponse = responseEntity.getBody();

            if (templateResponse == null || !templateResponse.isSuccess()) {
                throw new RuntimeException("Failed to render template: " + (templateResponse != null ? templateResponse.getError() : "null response"));
            }

            RenderResponseDto renderedData = templateResponse.getData();
            if (renderedData == null) {
                throw new RuntimeException("Rendered data came back empty.");
            }

            logger.debug("Sending push notification to token: {}", pushToken);

            PushRequest fcmRequest = new PushRequest();
            fcmRequest.setDeviceToken(pushToken);
            fcmRequest.setTitle(renderedData.getRenderedSubject());
            fcmRequest.setBody(renderedData.getRenderedBody());
            fcmRequest.setNotificationId(notificationId);

            fcmRequest.setImageUrl(renderedData.getRenderedImageUrl());
            fcmRequest.setActionLink(renderedData.getRenderedActionLink());

            fcmService.sendPushNotification(fcmRequest);

            logger.info("Successfully processed notificationId: {}", notificationId);
            reportStatus(notificationId, "delivered", null);
            redisTemplate.opsForValue().set(statusKey, "DELIVERED");

        } catch (Exception e) {
            logger.error("Failed to process notificationId {}: {}", notificationId, e.getMessage());
            reportStatus(notificationId, "failed", e.getMessage());
            redisTemplate.opsForValue().set(statusKey, "FAILED");
            throw new RuntimeException("Failing message processing to trigger retry", e);
        }
    }

    /**
     * Helper method to report the final status back to the API Gateway
     */
    private void reportStatus(String notificationId, String status, String error) {
        try {
            StatusUpdateDto statusUpdate = new StatusUpdateDto();
            statusUpdate.setNotificationId(notificationId);
            statusUpdate.setStatus(status);
            statusUpdate.setError(error);
            logger.debug("Reporting status to Gateway: {}", statusUpdate);

            // This endpoint is "fire and forget"
            restTemplate.postForObject(STATUS_UPDATE_URL, statusUpdate, Object.class);

        } catch (Exception e) {
            // If reporting fails, we can't do much. Just log it.
            logger.error("CRITICAL: Failed to report status for notificationId {}: {}",
                    notificationId, e.getMessage());
        }
    }
}