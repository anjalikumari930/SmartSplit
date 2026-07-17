package com.smartsplit.balance.dto;

import java.util.List;
import java.util.UUID;

/**
 * Represents the complete settlement plan for a group.
 * Contains all optimized transactions needed to settle debts.
 */
public record SettlementDetailResponse(
        UUID groupId,
        List<SettlementResponse> settlements) {
}
