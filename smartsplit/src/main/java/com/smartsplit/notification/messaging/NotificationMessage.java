package com.smartsplit.notification.messaging;

import java.math.BigDecimal;
import java.util.UUID;

public record NotificationMessage(
        String eventType,
        UUID userId,
        UUID groupId,
        String title,
        String message,
        BigDecimal amount) {
}
