package com.smartsplit.balance.utility;

import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.user.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementAlgorithmTest {

    @Test
    void simpleThreeWaySettlementProducesTwoTransactions() {
        SplitwiseSimplify algo = new SplitwiseSimplify();

        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();

        User userA = new User();
        userA.setId(a);
        userA.setName("A");
        User userB = new User();
        userB.setId(b);
        userB.setName("B");
        User userC = new User();
        userC.setId(c);
        userC.setName("C");

        Map<UUID, BigDecimal> balances = new HashMap<>();
        balances.put(a, new BigDecimal("-10.00"));
        balances.put(b, new BigDecimal("5.00"));
        balances.put(c, new BigDecimal("5.00"));

        Map<UUID, User> users = new HashMap<>();
        users.put(a, userA);
        users.put(b, userB);
        users.put(c, userC);

        List<SettlementResponse> settlements = algo.calculateSettlements(balances, users);

        assertEquals(2, settlements.size(), "Expected two settlement transactions");
        BigDecimal total = settlements.stream()
                .map(SettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("10.00"), total);
    }

    @Test
    void jaipurGroupScenarioEnsuresCreditorNeverPays() {
        SplitwiseSimplify algo = new SplitwiseSimplify();

        UUID name2Id = UUID.randomUUID(); // "You" - Creditor (+250)
        UUID bobId = UUID.randomUUID();   // Bob - Creditor (+3250)
        UUID name1Id = UUID.randomUUID(); // name1 - Debtor (-1750)
        UUID jiyaId = UUID.randomUUID();  // jiya123 - Debtor (-1750)

        User name2 = new User();
        name2.setId(name2Id);
        name2.setName("name2");

        User bob = new User();
        bob.setId(bobId);
        bob.setName("Bob");

        User name1 = new User();
        name1.setId(name1Id);
        name1.setName("name1");

        User jiya = new User();
        jiya.setId(jiyaId);
        jiya.setName("jiya123");

        Map<UUID, BigDecimal> balances = new HashMap<>();
        balances.put(name2Id, new BigDecimal("250.00"));
        balances.put(bobId, new BigDecimal("3250.00"));
        balances.put(name1Id, new BigDecimal("-1750.00"));
        balances.put(jiyaId, new BigDecimal("-1750.00"));

        Map<UUID, User> users = new HashMap<>();
        users.put(name2Id, name2);
        users.put(bobId, bob);
        users.put(name1Id, name1);
        users.put(jiyaId, jiya);

        List<SettlementResponse> settlements = algo.calculateSettlements(balances, users);

        // Expect exactly 3 transactions
        assertEquals(3, settlements.size(), "Expected exactly 3 transactions");

        // Total amount transferred across the group must be 3500.00
        BigDecimal totalTransferred = settlements.stream()
                .map(SettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("3500.00"), totalTransferred);

        // Crucial invariant: Creditors (name2 and Bob) must NEVER be fromUserId (paying)
        for (SettlementResponse s : settlements) {
            assertFalse(s.fromUserId().equals(name2Id), "name2 is a creditor and must NEVER pay money!");
            assertFalse(s.fromUserId().equals(bobId), "Bob is a creditor and must NEVER pay money!");
        }

        // Assert that name2 receives exactly 250.00
        BigDecimal name2Received = settlements.stream()
                .filter(s -> s.toUserId().equals(name2Id))
                .map(SettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("250.00"), name2Received);

        // Assert that Bob receives exactly 3250.00
        BigDecimal bobReceived = settlements.stream()
                .filter(s -> s.toUserId().equals(bobId))
                .map(SettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(new BigDecimal("3250.00"), bobReceived);
    }
}
