package com.smartsplit.notification.messaging;

import com.smartsplit.config.RabbitMqConfig;
import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMqConfig.NOTIFICATION_QUEUE)
    public void handle(NotificationMessage message) {
        notificationService.createNotification(
                message.userId(),
                message.title(),
                message.message(),
                NotificationType.IN_APP);
    }
}
