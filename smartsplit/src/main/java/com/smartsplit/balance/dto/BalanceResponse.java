package com.smartsplit.balance.dto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents the balance for a single user in a group.
 * Positive balance means user is owed money.
 * Negative balance means user owes money.
 */
public record BalanceResponse(
        UUID userId,
        String userName,
        BigDecimal balance) {
}
