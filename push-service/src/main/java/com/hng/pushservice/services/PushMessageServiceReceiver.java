package com.hng.pushservice.services;

import com.hng.pushservice.dto.PushMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RabbitListener(queues = "push.queue")
@RequiredArgsConstructor
public class PushMessageServiceReceiver {

    private final Logger logger = LoggerFactory.getLogger(PushMessageServiceReceiver.class);

    private final FcmService fcmService;
    private final StringRedisTemplate redisTemplate;

    @RabbitHandler
    public void consumePushQueue(PushMessage pushMessage){
        // Message from RabbitMQ received - LOG.INFO
        logger.info("push message received for MQ: {}", pushMessage);

        try{
            // Update Redis
            // check
            if (pushMessage.getNotificationId() == null || pushMessage.getNotificationId().isEmpty()){
                logger.warn("Notification Id is null or empty");
                return;
            }

            String redisKey = pushMessage.getNotificationId();
            String currentStatus = redisTemplate.opsForValue().get(redisKey);

            if (currentStatus.equalsIgnoreCase("SENT")){
                logger.warn("NotificationId already processed!");
                return;
            }

            updateStatus(redisKey, "PROCESSING");
            logger.debug("Updated status in redis for {} to PROCESSING", redisKey);

            fcmService.sendPushNotification(pushMessage);
            updateStatus(redisKey, "SENT");
            logger.info("Push notification sent for {}", redisKey);
        }catch(Exception ex){
            logger.error("Could not process notificationId: {}. Message: {}", pushMessage.getNotificationId(), ex.getMessage());
            String redisKey = "notification:" + pushMessage.getNotificationId();
            updateStatus(redisKey, "FAILED");
            throw new RuntimeException("Failing Message. Triggering retry logic");
        }

    }

    // Helper method to update Redis
    private void updateStatus(String notificationId, String status){
        String key = "notification:"+ notificationId;
        redisTemplate.opsForValue().set(key, status);
    }
}
