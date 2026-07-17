package com.smartsplit.balance.utility;

import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Settlement Algorithm implementation using Greedy approach.
 * 
 * Algorithm:
 * 1. Separate users into creditors (positive balance) and debtors (negative
 * balance)
 * 2. Iterate through debtors and match them with creditors
 * 3. For each debtor, settle with creditors until debtor is settled or no
 * creditors left
 * 4. This minimizes the number of transactions needed
 * 
 * Time Complexity: O(n^2) where n is number of users with non-zero balance
 * Space Complexity: O(n)
 */
@Slf4j
@Component
public class SettlementAlgorithm {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;

    /**
     * Calculate optimized settlements for minimizing transactions.
     * 
     * @param balances Map of userId to balance amount
     * @param users    Map of userId to User object
     * @return List of settlement transactions
     */
    public List<SettlementResponse> calculateSettlements(
            Map<UUID, BigDecimal> balances,
            Map<UUID, User> users) {

        List<SettlementResponse> settlements = new ArrayList<>();

        if (balances == null || balances.isEmpty()) {
            return settlements;
        }

        // Create mutable copies for manipulation during algorithm
        Map<UUID, BigDecimal> debtorBalances = new HashMap<>();
        Map<UUID, BigDecimal> creditorBalances = new HashMap<>();

        // Separate debtors and creditors
        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            BigDecimal balance = entry.getValue().setScale(SCALE, RoundingMode.HALF_UP);

            // Skip zero balances
            if (balance.compareTo(ZERO) == 0) {
                continue;
            }

            if (balance.compareTo(ZERO) < 0) {
                // Negative balance = debtor (owes money)
                debtorBalances.put(entry.getKey(), balance.abs());
            } else {
                // Positive balance = creditor (owed money)
                creditorBalances.put(entry.getKey(), balance);
            }
        }

        // Sort for deterministic results
        List<UUID> debtors = new ArrayList<>(debtorBalances.keySet());
        List<UUID> creditors = new ArrayList<>(creditorBalances.keySet());
        debtors.sort(Comparator.naturalOrder());
        creditors.sort(Comparator.naturalOrder());

        // Greedy matching: match each debtor with creditors
        for (UUID debtorId : debtors) {
            BigDecimal debtorOwed = debtorBalances.get(debtorId);

            for (UUID creditorId : creditors) {
                if (debtorOwed.compareTo(ZERO) <= 0) {
                    break;
                }

                BigDecimal creditorCredit = creditorBalances.get(creditorId);
                if (creditorCredit.compareTo(ZERO) <= 0) {
                    continue;
                }

                // Settle as much as possible between this pair
                BigDecimal settlementAmount = debtorOwed.min(creditorCredit)
                        .setScale(SCALE, RoundingMode.HALF_UP);

                if (settlementAmount.compareTo(ZERO) > 0) {
                    settlements.add(
                            new SettlementResponse(
                                    debtorId,
                                    users.get(debtorId).getName(),
                                    creditorId,
                                    users.get(creditorId).getName(),
                                    settlementAmount));

                    // Update remaining balances
                    debtorOwed = debtorOwed.subtract(settlementAmount);
                    creditorBalances.put(
                            creditorId,
                            creditorCredit.subtract(settlementAmount));
                }
            }

            // Update debtor balance
            debtorBalances.put(debtorId, debtorOwed);
        }

        log.info("Calculated {} settlement transactions", settlements.size());
        return settlements;
    }
}
