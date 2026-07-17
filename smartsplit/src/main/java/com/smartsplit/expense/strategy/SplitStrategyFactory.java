package com.smartsplit.expense.strategy;

import com.smartsplit.expense.SplitType;
import com.smartsplit.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SplitStrategyFactory {

    private final EqualSplitStrategy equalSplitStrategy;
    private final ExactSplitStrategy exactSplitStrategy;
    private final PercentageSplitStrategy percentageSplitStrategy;

    public SplitStrategy getStrategy(SplitType splitType) {
        return switch (splitType) {
            case EQUAL -> equalSplitStrategy;
            case EXACT -> exactSplitStrategy;
            case PERCENTAGE -> percentageSplitStrategy;
            default -> throw new BadRequestException("Unsupported split type: " + splitType);
        };
    }
}
