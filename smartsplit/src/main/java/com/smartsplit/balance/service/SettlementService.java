package com.smartsplit.balance.service;

import com.smartsplit.balance.dto.SettlementDetailResponse;
import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.balance.entity.Settlement;
import com.smartsplit.balance.repository.SettlementRepository;
import com.smartsplit.balance.utility.SplitwiseSimplify;
import com.smartsplit.exception.ResourceNotFoundException;
import com.smartsplit.group.Group;
import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.config.RabbitMqConfig;
import com.smartsplit.notification.messaging.NotificationMessage;
import com.smartsplit.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
        private final SplitwiseSimplify splitwiseSimplify;
        private final GroupRepository groupRepository;
        private final GroupMemberRepository groupMemberRepository;
        private final SettlementRepository settlementRepository;
        private final RabbitTemplate rabbitTemplate;

        private static final BigDecimal ZERO = BigDecimal.ZERO;

        /**
         * Get optimized settlement plan for a group.
         * 
         * @param groupId UUID of the group
         * @return SettlementDetailResponse containing all settlement transactions
         * @throws ResourceNotFoundException if group not found
         */
        @Transactional
        public SettlementDetailResponse getGroupSettlements(UUID groupId) {
                // Validate group exists
                Group group = groupRepository.findById(groupId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Group not found with id: " + groupId));

                // Get all group members
                List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(groupId);
                if (groupMembers.isEmpty()) {
                        log.warn("Group {} has no members", groupId);
                        settlementRepository.deleteUnsettledByGroupId(groupId);
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

                // Calculate optimized settlements (SplitwiseSimplify)
                List<SettlementResponse> settlements = splitwiseSimplify.calculateSettlements(balances, userMap);
                persistSettlements(group, settlements, userMap);

                log.info("Calculated {} settlements for group {}", settlements.size(), groupId);
                return new SettlementDetailResponse(groupId, settlements);
        }

        private void persistSettlements(Group group, List<SettlementResponse> settlements, Map<UUID, User> userMap) {
                settlementRepository.deleteUnsettledByGroupId(group.getId());

                for (SettlementResponse settlementResponse : settlements) {
                        User payer = userMap.get(settlementResponse.fromUserId());
                        User payee = userMap.get(settlementResponse.toUserId());

                        if (payer == null || payee == null) {
                                throw new ResourceNotFoundException(
                                                "Settlement participant could not be resolved for group: "
                                                                + group.getId());
                        }

                        Settlement settlement = new Settlement();
                        settlement.setGroup(group);
                        settlement.setPayer(payer);
                        settlement.setPayee(payee);
                        settlement.setAmount(settlementResponse.amount());
                        settlement.setCreatedAt(LocalDateTime.now());
                        settlement.setIsSettled(false);
                        settlementRepository.save(settlement);
                }
        }

        /**
         * Get settlement transactions where a specific user is involved.
         * 
         * @param groupId UUID of the group
         * @param userId  UUID of the user
         * @return SettlementDetailResponse containing settlements involving the user
         * @throws ResourceNotFoundException if group or user not found
         */
        @Transactional
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

                publishNotification(
                                "SETTLEMENT_COMPLETED",
                                savedSettlement.getPayer().getId(),
                                savedSettlement.getGroup().getId(),
                                "Settlement Completed",
                                String.format("You have settled $%.2f with %s.",
                                                savedSettlement.getAmount(),
                                                savedSettlement.getPayee().getName()),
                                savedSettlement.getAmount());

                publishNotification(
                                "SETTLEMENT_RECEIVED",
                                savedSettlement.getPayee().getId(),
                                savedSettlement.getGroup().getId(),
                                "Settlement Received",
                                String.format("%s has settled $%.2f with you.",
                                                savedSettlement.getPayer().getName(),
                                                savedSettlement.getAmount()),
                                savedSettlement.getAmount());

                log.info("Settlement {} marked as completed", settlementId);
        }

        private void publishNotification(String eventType, UUID userId, UUID groupId, String title,
                        String message, BigDecimal amount) {
                NotificationMessage notificationMessage = new NotificationMessage(
                                eventType,
                                userId,
                                groupId,
                                title,
                                message,
                                amount);

                rabbitTemplate.convertAndSend(
                                RabbitMqConfig.EXCHANGE_NAME,
                                RabbitMqConfig.NOTIFICATION_ROUTING_KEY,
                                notificationMessage);
        }
}
