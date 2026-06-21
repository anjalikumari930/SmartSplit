package com.smartsplit.balance.dto;

import java.util.List;
import java.util.UUID;

/**
 * Represents the complete balance summary for a group.
 * Contains all balances for all group members.
 */
public record BalanceDetailResponse(
        UUID groupId,
        List<BalanceResponse> balances) {
}
