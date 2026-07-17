package com.smartsplit.notification.listener;

import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.event.SettlementCompletedEvent;
import com.smartsplit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SettlementNotificationListener {

    private final NotificationService notificationService;

    @EventListener
    @Async
    public void onSettlementCompleted(SettlementCompletedEvent event) {
        // Notify payer
        String payerTitle = "Settlement Completed";
        String payerMessage = String.format(
                "You have settled $%.2f with %s.",
                event.getAmount(),
                event.getPayeeName());

        notificationService.createNotification(
                event.getPayerUserId(),
                payerTitle,
                payerMessage,
                NotificationType.IN_APP);

        // Notify payee
        String payeeTitle = "Settlement Received";
        String payeeMessage = String.format(
                "%s has settled $%.2f with you.",
                event.getPayerName(),
                event.getAmount());

        notificationService.createNotification(
                event.getPayeeUserId(),
                payeeTitle,
                payeeMessage,
                NotificationType.IN_APP);
    }
}
