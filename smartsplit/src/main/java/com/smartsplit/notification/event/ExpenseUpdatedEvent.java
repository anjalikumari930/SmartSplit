package com.smartsplit.notification.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class ExpenseUpdatedEvent extends ApplicationEvent {
    private final UUID expenseId;
    private final String description;
    private final BigDecimal amount;
    private final UUID paidByUserId;
    private final UUID groupId;
    private final String paidByUserName;

    public ExpenseUpdatedEvent(
            Object source,
            UUID expenseId,
            String description,
            BigDecimal amount,
            UUID paidByUserId,
            UUID groupId,
            String paidByUserName) {
        super(source);
        this.expenseId = expenseId;
        this.description = description;
        this.amount = amount;
        this.paidByUserId = paidByUserId;
        this.groupId = groupId;
        this.paidByUserName = paidByUserName;
    }

    public UUID getExpenseId() {
        return expenseId;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public UUID getPaidByUserId() {
        return paidByUserId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getPaidByUserName() {
        return paidByUserName;
    }
}
