package com.hng.pushservice.services;

import com.hng.pushservice.dto.PushMessage;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RabbitListener(queues = "push.queue")
public class PushMessageServiceReceiver {

    @RabbitHandler
    public void consumePushQueue(PushMessage pushMessage){
        System.out.println("push message received for MQ: " +pushMessage.getPushToken());
    }
}
