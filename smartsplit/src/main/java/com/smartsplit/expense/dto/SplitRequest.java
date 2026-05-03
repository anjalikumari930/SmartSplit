package com.smartsplit.expense.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SplitRequest(
        @NotNull(message = "User ID is required for each split") UUID userId,
        @NotNull(message = "Split value is required") @DecimalMin(value = "0.00", inclusive = false, message = "Split value must be greater than zero") BigDecimal value) {
}
