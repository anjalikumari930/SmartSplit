package com.smartsplit.expense.dto;

import com.smartsplit.expense.SplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ExpenseRequest(
        @NotBlank(message = "Description is required") String description,
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be greater than zero") BigDecimal amount,
        @NotNull(message = "Paid by user ID is required") UUID paidByUserId,
        @NotNull(message = "Group ID is required") UUID groupId,
        @NotNull(message = "Split type is required") SplitType splitType,
        @NotEmpty(message = "Splits are required") @Valid List<SplitRequest> splits) {
}
