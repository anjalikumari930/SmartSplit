package com.smartsplit.expense.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitResponse(
        UUID userId,
        BigDecimal amountOwed) {
}
