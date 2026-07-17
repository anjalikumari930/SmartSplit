package com.smartsplit.notification.dto;

import com.smartsplit.notification.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String title,
        String message,
        NotificationType type,
        Boolean isRead,
        LocalDateTime createdAt) {
}
