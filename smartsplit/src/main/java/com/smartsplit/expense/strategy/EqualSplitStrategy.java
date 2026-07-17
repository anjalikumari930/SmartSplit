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
public class EqualSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount,
            List<SplitRequest> splitRequests,
            Map<UUID, User> usersById) {
        if (splitRequests.isEmpty()) {
            throw new BadRequestException("At least one split participant is required for equal split.");
        }

        int participantCount = splitRequests.size();
        BigDecimal equalAmount = totalAmount.divide(BigDecimal.valueOf(participantCount), 2, RoundingMode.DOWN);
        BigDecimal distributedAmount = equalAmount.multiply(BigDecimal.valueOf(participantCount));
        BigDecimal remainder = totalAmount.subtract(distributedAmount);

        List<Split> splits = new ArrayList<>();

        for (int index = 0; index < participantCount; index++) {
            SplitRequest request = splitRequests.get(index);
            User user = usersById.get(request.userId());
            if (user == null) {
                throw new BadRequestException("User not found for split participant: " + request.userId());
            }

            BigDecimal amountOwed = equalAmount;
            if (index == 0) {
                amountOwed = amountOwed.add(remainder);
            }

            Split split = new Split();
            split.setUser(user);
            split.setAmountOwed(amountOwed);
            splits.add(split);
        }

        return splits;
    }
}
