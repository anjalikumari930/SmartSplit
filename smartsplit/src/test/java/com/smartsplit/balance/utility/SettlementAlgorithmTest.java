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

import com.smartsplit.balance.utility.SplitwiseSimplify;

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
}
