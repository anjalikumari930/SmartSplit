package com.smartsplit.balance.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a single settlement transaction.
 * Indicates that fromUser owes toUser the specified amount.
 */
public record SettlementResponse(
        UUID fromUserId,
        String fromUserName,
        UUID toUserId,
        String toUserName,
        BigDecimal amount) {
}
