package com.smartsplit.notification.event;

import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;
import java.util.UUID;

public class SettlementCompletedEvent extends ApplicationEvent {
    private final UUID settlementId;
    private final UUID payerUserId;
    private final UUID payeeUserId;
    private final BigDecimal amount;
    private final String payerName;
    private final String payeeName;

    public SettlementCompletedEvent(
            Object source,
            UUID settlementId,
            UUID payerUserId,
            UUID payeeUserId,
            BigDecimal amount,
            String payerName,
            String payeeName) {
        super(source);
        this.settlementId = settlementId;
        this.payerUserId = payerUserId;
        this.payeeUserId = payeeUserId;
        this.amount = amount;
        this.payerName = payerName;
        this.payeeName = payeeName;
    }

    public UUID getSettlementId() {
        return settlementId;
    }

    public UUID getPayerUserId() {
        return payerUserId;
    }

    public UUID getPayeeUserId() {
        return payeeUserId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPayerName() {
        return payerName;
    }

    public String getPayeeName() {
        return payeeName;
    }
}
