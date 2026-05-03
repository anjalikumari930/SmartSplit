package com.smartsplit.expense.strategy;

import com.smartsplit.expense.dto.SplitRequest;
import com.smartsplit.expense.entity.Split;
import com.smartsplit.user.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface SplitStrategy {
    List<Split> calculateSplits(BigDecimal totalAmount,
            List<SplitRequest> splitRequests,
            Map<UUID, User> usersById);
}
