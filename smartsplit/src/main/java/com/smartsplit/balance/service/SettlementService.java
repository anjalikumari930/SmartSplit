package com.smartsplit.balance.service;

import com.smartsplit.balance.dto.SettlementDetailResponse;
import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.balance.entity.Settlement;
import com.smartsplit.balance.repository.SettlementRepository;
import com.smartsplit.balance.utility.SettlementAlgorithm;
import com.smartsplit.exception.ResourceNotFoundException;
import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.notification.event.SettlementCompletedEvent;
import com.smartsplit.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for calculating optimized settlements in a group.
 * 
 * Settlement Calculation Logic:
 * - Fetch all balances from BalanceService
 * - Use SettlementAlgorithm to minimize number of transactions
 * - Return optimized settlement list
 * 
 * Example:
 * A +500, B -300, C -200
 * Settlements: B → A: 300, C → A: 200
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {

        private final BalanceService balanceService;
        private final SettlementAlgorithm settlementAlgorithm;
        private final GroupRepository groupRepository;
        private final GroupMemberRepository groupMemberRepository;
        private final SettlementRepository settlementRepository;
        private final ApplicationEventPublisher eventPublisher;

        private static final BigDecimal ZERO = BigDecimal.ZERO;

        /**
         * Get optimized settlement plan for a group.
         * 
         * @param groupId UUID of the group
         * @return SettlementDetailResponse containing all settlement transactions
         * @throws ResourceNotFoundException if group not found
         */
        @Transactional(readOnly = true)
        public SettlementDetailResponse getGroupSettlements(UUID groupId) {
                // Validate group exists
                groupRepository.findById(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Group not found with id: " + groupId));

                // Get all group members
                List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
                if (groupMembers.isEmpty()) {
                        log.warn("Group {} has no members", groupId);
                        return new SettlementDetailResponse(groupId, List.of());
                }

                // Get all balances
                var balanceDetail = balanceService.getGroupBalances(groupId);
                Map<UUID, BigDecimal> balances = balanceDetail.balances().stream()
                                .collect(Collectors.toMap(
                                                b -> b.userId(),
                                                b -> b.balance()));

                // Build user map for settlement response
                Map<UUID, User> userMap = groupMembers.stream()
                                .collect(Collectors.toMap(
                                                gm -> gm.getUser().getId(),
                                                GroupMember::getUser));

                // Calculate optimized settlements
                List<SettlementResponse> settlements = settlementAlgorithm.calculateSettlements(balances, userMap);

                log.info("Calculated {} settlements for group {}", settlements.size(), groupId);
                return new SettlementDetailResponse(groupId, settlements);
        }

        /**
         * Get settlement transactions where a specific user is involved.
         * 
         * @param groupId UUID of the group
         * @param userId  UUID of the user
         * @return SettlementDetailResponse containing settlements involving the user
         * @throws ResourceNotFoundException if group or user not found
         */
        @Transactional(readOnly = true)
        public SettlementDetailResponse getUserSettlements(UUID groupId, UUID userId) {
                // Validate group exists
                groupRepository.findById(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Group not found with id: " + groupId));

                // Validate user is member of group
                groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of the group"));

                // Get all settlements for group
                SettlementDetailResponse allSettlements = getGroupSettlements(groupId);

                // Filter settlements involving this user
                List<SettlementResponse> userSettlements = allSettlements.settlements().stream()
                                .filter(s -> s.fromUserId().equals(userId) || s.toUserId().equals(userId))
                                .collect(Collectors.toList());

                log.info("Retrieved {} settlements for user {} in group {}", userSettlements.size(), userId, groupId);
                return new SettlementDetailResponse(groupId, userSettlements);
        }

        /**
         * Mark a settlement as completed.
         * 
         * @param settlementId UUID of the settlement
         * @throws ResourceNotFoundException if settlement not found or already settled
         */
        @Transactional
        public void settlePayment(UUID settlementId) {
                Settlement settlement = settlementRepository.findByIdAndIsSettledFalse(settlementId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Settlement not found or already settled with id: " + settlementId));

                settlement.setIsSettled(true);
                settlement.setSettledAt(LocalDateTime.now());

                Settlement savedSettlement = settlementRepository.save(settlement);

                // Publish event
                SettlementCompletedEvent event = new SettlementCompletedEvent(
                                this,
                                savedSettlement.getId(),
                                savedSettlement.getPayer().getId(),
                                savedSettlement.getPayee().getId(),
                                savedSettlement.getAmount(),
                                savedSettlement.getPayer().getName(),
                                savedSettlement.getPayee().getName());
                eventPublisher.publishEvent(event);

                log.info("Settlement {} marked as completed", settlementId);
        }
}
