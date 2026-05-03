package com.smartsplit.expense.strategy;

import com.smartsplit.expense.dto.SplitRequest;
import com.smartsplit.expense.entity.Split;
import com.smartsplit.exception.BadRequestException;
import com.smartsplit.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExactSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount,
            List<SplitRequest> splitRequests,
            Map<UUID, User> usersById) {
        if (splitRequests.isEmpty()) {
            throw new BadRequestException("At least one split participant is required for exact split.");
        }

        List<Split> splits = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        for (SplitRequest request : splitRequests) {
            User user = usersById.get(request.userId());
            if (user == null) {
                throw new BadRequestException("User not found for split participant: " + request.userId());
            }

            BigDecimal amountOwed = request.value().setScale(2, RoundingMode.HALF_UP);
            sum = sum.add(amountOwed);

            Split split = new Split();
            split.setUser(user);
            split.setAmountOwed(amountOwed);
            splits.add(split);
        }

        if (sum.compareTo(totalAmount.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BadRequestException("Exact split values must sum to the total expense amount.");
        }

        return splits;
    }
}
