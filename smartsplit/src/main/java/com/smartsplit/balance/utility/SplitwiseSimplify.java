package com.smartsplit.balance.utility;

import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.user.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class SplitwiseSimplify {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;

    public List<SettlementResponse> calculateSettlements(Map<UUID, BigDecimal> balances,
            Map<UUID, User> users) {
        // Build compact lists
        List<UUID> ids = new ArrayList<>();
        List<BigDecimal> list = new ArrayList<>();
        for (Map.Entry<UUID, BigDecimal> e : balances.entrySet()) {
            BigDecimal v = e.getValue().setScale(SCALE, RoundingMode.HALF_UP);
            if (v.compareTo(ZERO) != 0) {
                ids.add(e.getKey());
                list.add(v);
            }
        }

        BigDecimal[] debts = list.toArray(new BigDecimal[0]);
        List<SettlementResponse> current = new ArrayList<>();
        List<SettlementResponse> best = new ArrayList<>();

        dfsIndex(0, debts, ids, users, current, best);
        return best;
    }

    private int dfsIndex(int cur, BigDecimal[] balances, List<UUID> ids, Map<UUID, User> users,
            List<SettlementResponse> current, List<SettlementResponse> best) {

        if (!best.isEmpty() && current.size() >= best.size())
            return Integer.MAX_VALUE;

        int n = balances.length;
        while (cur < n && balances[cur].compareTo(ZERO) == 0)
            cur++;
        if (cur == n) {
            if (best.isEmpty() || current.size() < best.size()) {
                best.clear();
                best.addAll(new ArrayList<>(current));
            }
            return 0;
        }

        int min = Integer.MAX_VALUE;
        Set<BigDecimal> tried = new HashSet<>();

        for (int next = cur + 1; next < n; next++) {
            if (balances[next].compareTo(ZERO) == 0)
                continue;
            if (balances[cur].signum() + balances[next].signum() != 0)
                continue;

            BigDecimal key = balances[next];
            if (tried.contains(key))
                continue;
            tried.add(key);

            BigDecimal transfer = balances[cur].abs().min(balances[next].abs()).setScale(SCALE, RoundingMode.HALF_UP);

            UUID fromId;
            String fromName;
            UUID toId;
            String toName;
            if (balances[cur].compareTo(ZERO) < 0) {
                fromId = ids.get(cur);
                toId = ids.get(next);
            } else {
                fromId = ids.get(next);
                toId = ids.get(cur);
            }

            fromName = users.get(fromId).getName();
            toName = users.get(toId).getName();

            current.add(new SettlementResponse(fromId, fromName, toId, toName, transfer));

            BigDecimal beforeNext = balances[next];
            balances[next] = balances[next].add(balances[cur]).setScale(SCALE, RoundingMode.HALF_UP);

            dfsIndex(cur + 1, balances, ids, users, current, best);

            current.remove(current.size() - 1);
            balances[next] = beforeNext;

            if (balances[cur].add(beforeNext).compareTo(ZERO) == 0)
                break;
        }

        return min == Integer.MAX_VALUE ? 0 : min;
    }
}
