package com.smartsplit.balance.service;

import com.smartsplit.balance.dto.BalanceDetailResponse;
import com.smartsplit.balance.dto.BalanceResponse;
import com.smartsplit.exception.ResourceNotFoundException;
import com.smartsplit.expense.entity.Expense;
import com.smartsplit.expense.repository.ExpenseRepository;
import com.smartsplit.group.Group;
import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.user.User;
import com.smartsplit.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for calculating balances in a group.
 * 
 * Balance Calculation Logic:
 * - For each expense: payer gets credited with full amount
 * - Each participant in split gets debited by their owed amount
 * 
 * Example:
 * A paid 1000, B owes 500, C owes 500
 * Balances: A = +1000, B = -500, C = -500
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final ExpenseRepository expenseRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final int SCALE = 2;

    /**
     * Get balance summary for a group.
     * 
     * @param groupId UUID of the group
     * @return BalanceDetailResponse containing all user balances in the group
     * @throws ResourceNotFoundException if group not found
     */
    @Transactional(readOnly = true)
    public BalanceDetailResponse getGroupBalances(UUID groupId) {
        // Validate group exists
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Get all group members
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
        if (groupMembers.isEmpty()) {
            log.warn("Group {} has no members", groupId);
            return new BalanceDetailResponse(groupId, List.of());
        }

        // Calculate balances
        Map<UUID, BigDecimal> balances = calculateBalances(groupId);

        // Build response
        List<BalanceResponse> balanceResponses = groupMembers.stream()
                .map(member -> {
                    User user = member.getUser();
                    BigDecimal balance = balances.getOrDefault(user.getId(), ZERO);
                    return new BalanceResponse(
                            user.getId(),
                            user.getName(),
                            balance.setScale(SCALE, RoundingMode.HALF_UP));
                })
                .collect(Collectors.toList());

        log.info("Retrieved balances for group {} with {} members", groupId, balanceResponses.size());
        return new BalanceDetailResponse(groupId, balanceResponses);
    }

    /**
     * Get individual user balance in a group.
     * 
     * @param groupId UUID of the group
     * @param userId  UUID of the user
     * @return BalanceResponse for the user
     * @throws ResourceNotFoundException if group or user not found
     */
    @Transactional(readOnly = true)
    public BalanceResponse getUserBalance(UUID groupId, UUID userId) {
        // Validate group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Validate user is member of group
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the group"));

        // Calculate balance
        Map<UUID, BigDecimal> balances = calculateBalances(groupId);
        BigDecimal userBalance = balances.getOrDefault(userId, ZERO)
                .setScale(SCALE, RoundingMode.HALF_UP);

        log.info("Retrieved balance for user {} in group {}: {}", userId, groupId, userBalance);
        return new BalanceResponse(userId, user.getName(), userBalance);
    }

    /**
     * Calculate net balances for all users in a group.
     * 
     * Algorithm:
     * 1. For each expense in group:
     * - Payer gets +amount
     * - Each split participant gets -amountOwed
     * 
     * @param groupId UUID of the group
     * @return Map of userId to net balance
     */
    private Map<UUID, BigDecimal> calculateBalances(UUID groupId) {
        // Fetch all expenses for the group
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);

        Map<UUID, BigDecimal> balances = new HashMap<>();

        for (Expense expense : expenses) {
            // Credit the payer with full amount
            UUID payerId = expense.getPaidBy().getId();
            BigDecimal amount = expense.getAmount().setScale(SCALE, RoundingMode.HALF_UP);

            balances.merge(payerId, amount, BigDecimal::add);

            // Debit each participant by their owed amount
            expense.getSplits().forEach(split -> {
                UUID participantId = split.getUser().getId();
                BigDecimal owed = split.getAmountOwed().setScale(SCALE, RoundingMode.HALF_UP);

                balances.merge(participantId, owed.negate(), BigDecimal::add);
            });
        }

        // Round all balances
        balances.replaceAll((k, v) -> v.setScale(SCALE, RoundingMode.HALF_UP));

        log.debug("Calculated balances for group {}: {}", groupId, balances);
        return balances;
    }
}
