package com.smartsplit.expense.dto;

import com.smartsplit.expense.SplitType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        UUID paidBy,
        UUID groupId,
        SplitType splitType,
        List<SplitResponse> splits,
        LocalDateTime createdAt) {
}
