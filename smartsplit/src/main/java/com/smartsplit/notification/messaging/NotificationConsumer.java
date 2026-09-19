package com.smartsplit.notification.messaging;

import com.smartsplit.config.RabbitMqConfig;
import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationMessage message) {
        try {
            notificationService.createNotification(
                    message.userId(),
                    message.title(),
                    message.message(),
                    NotificationType.IN_APP);
        } catch (Exception ex) {
            log.error("Failed to process notification message: {}", message, ex);
            throw new AmqpRejectAndDontRequeueException("Failed to process notification", ex);
        }
    }
}
