package com.hng.pushservice.services;

import com.hng.pushservice.dto.PushMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RabbitListener(queues = "push.queue")
@RequiredArgsConstructor
public class PushMessageServiceReceiver {

    private final StringRedisTemplate redisTemplate;

    @RabbitHandler
    public void consumePushQueue(PushMessage pushMessage){
        // Message from RabbitMQ received - LOG.INFO
        System.out.println("push message received for MQ: " +pushMessage.getPushToken());

        // Update Redis
        String redisKey = "notification:" + pushMessage.getNotificationId();
        redisTemplate.opsForValue().set(redisKey, "PROCESSING");

    }

    // Helper method to update Redis
    private void updateStatus(String notifcationId, String status){
        String key = "notification:"+ notifcationId;

        redisTemplate.opsForValue().set(key, status);
    }
}
