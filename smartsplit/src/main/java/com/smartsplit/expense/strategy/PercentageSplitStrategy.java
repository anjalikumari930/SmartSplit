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
public class PercentageSplitStrategy implements SplitStrategy {

    @Override
    public List<Split> calculateSplits(BigDecimal totalAmount,
            List<SplitRequest> splitRequests,
            Map<UUID, User> usersById) {
        if (splitRequests.isEmpty()) {
            throw new BadRequestException("At least one split participant is required for percentage split.");
        }

        BigDecimal percentageSum = BigDecimal.ZERO;
        List<Split> splits = new ArrayList<>();

        for (SplitRequest request : splitRequests) {
            percentageSum = percentageSum.add(request.value());
        }

        if (percentageSum.compareTo(BigDecimal.valueOf(100)) != 0) {
            throw new BadRequestException("Sum of percentage split values must equal 100.");
        }

        BigDecimal distributed = BigDecimal.ZERO;
        for (int index = 0; index < splitRequests.size(); index++) {
            SplitRequest request = splitRequests.get(index);
            User user = usersById.get(request.userId());
            if (user == null) {
                throw new BadRequestException("User not found for split participant: " + request.userId());
            }

            BigDecimal amountOwed = totalAmount
                    .multiply(request.value())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.DOWN);

            distributed = distributed.add(amountOwed);
            Split split = new Split();
            split.setUser(user);
            split.setAmountOwed(amountOwed);
            splits.add(split);
        }

        BigDecimal remainder = totalAmount.subtract(distributed);
        if (!remainder.equals(BigDecimal.ZERO)) {
            Split firstSplit = splits.get(0);
            firstSplit.setAmountOwed(firstSplit.getAmountOwed().add(remainder));
        }

        return splits;
    }
}
