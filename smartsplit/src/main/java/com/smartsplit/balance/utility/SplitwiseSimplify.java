package com.smartsplit.balance.utility;

import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.UUID;

/**
 * Utility for calculating optimized settlements in a group using the
 * Greedy Minimum Cash Flow Algorithm with exact-match pre-processing.
 *
 * Guarantees:
 * 1. Debtors (negative balance) ONLY pay money.
 * 2. Creditors (positive balance) ONLY receive money.
 * 3. Never shifts or contaminates one user's debt into another member's account.
 * 4. Minimizes the total number of transactions required to settle all debts (<= N - 1).
 */
@Component
public class SplitwiseSimplify {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal EPSILON = new BigDecimal("0.009");
    private static final int SCALE = 2;

    private static class Participant {
        final UUID userId;
        final String name;
        BigDecimal amount;

        Participant(UUID userId, String name, BigDecimal amount) {
            this.userId = userId;
            this.name = name;
            this.amount = amount;
        }
    }

    public List<SettlementResponse> calculateSettlements(Map<UUID, BigDecimal> balances,
            Map<UUID, User> users) {

        if (balances == null || balances.isEmpty()) {
            return List.of();
        }

        List<Participant> debtorList = new ArrayList<>();
        List<Participant> creditorList = new ArrayList<>();

        for (Map.Entry<UUID, BigDecimal> entry : balances.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            BigDecimal val = entry.getValue().setScale(SCALE, RoundingMode.HALF_UP);
            if (val.abs().compareTo(EPSILON) <= 0) {
                continue;
            }

            User user = (users != null) ? users.get(entry.getKey()) : null;
            String name = (user != null && user.getName() != null) ? user.getName() : "Member";

            if (val.compareTo(ZERO) > 0) {
                creditorList.add(new Participant(entry.getKey(), name, val));
            } else {
                debtorList.add(new Participant(entry.getKey(), name, val.abs()));
            }
        }

        List<SettlementResponse> settlements = new ArrayList<>();

        // Optimization 1: Exact matches (1 transaction directly resolves 1 debtor + 1 creditor)
        Iterator<Participant> debtorIter = debtorList.iterator();
        while (debtorIter.hasNext()) {
            Participant debtor = debtorIter.next();
            Iterator<Participant> creditorIter = creditorList.iterator();
            while (creditorIter.hasNext()) {
                Participant creditor = creditorIter.next();
                if (debtor.amount.compareTo(creditor.amount) == 0) {
                    settlements.add(new SettlementResponse(
                            debtor.userId,
                            debtor.name,
                            creditor.userId,
                            creditor.name,
                            debtor.amount));
                    debtorIter.remove();
                    creditorIter.remove();
                    break;
                }
            }
        }

        // Optimization 2: Greedy Min-Cash-Flow on remaining balances (Max-Heaps)
        PriorityQueue<Participant> debtors = new PriorityQueue<>((a, b) -> b.amount.compareTo(a.amount));
        PriorityQueue<Participant> creditors = new PriorityQueue<>((a, b) -> b.amount.compareTo(a.amount));

        debtors.addAll(debtorList);
        creditors.addAll(creditorList);

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Participant debtor = debtors.poll();
            Participant creditor = creditors.poll();

            BigDecimal transfer = debtor.amount.min(creditor.amount).setScale(SCALE, RoundingMode.HALF_UP);

            settlements.add(new SettlementResponse(
                    debtor.userId,
                    debtor.name,
                    creditor.userId,
                    creditor.name,
                    transfer));

            BigDecimal debtorRemaining = debtor.amount.subtract(transfer);
            BigDecimal creditorRemaining = creditor.amount.subtract(transfer);

            if (debtorRemaining.compareTo(EPSILON) > 0) {
                debtor.amount = debtorRemaining;
                debtors.add(debtor);
            }
            if (creditorRemaining.compareTo(EPSILON) > 0) {
                creditor.amount = creditorRemaining;
                creditors.add(creditor);
            }
        }

        return settlements;
    }
}
