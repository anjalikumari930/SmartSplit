package com.smartsplit.balance.controller;

import com.smartsplit.balance.dto.BalanceDetailResponse;
import com.smartsplit.balance.dto.BalanceResponse;
import com.smartsplit.balance.dto.SettlementDetailResponse;
import com.smartsplit.balance.service.BalanceService;
import com.smartsplit.balance.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * REST Controller for Balance and Settlement operations.
 * 
 * Endpoints:
 * - GET /groups/{groupId}/balances - Get all balances in group
 * - GET /groups/{groupId}/balances/{userId} - Get user balance in group
 * - GET /groups/{groupId}/settlements - Get optimized settlement plan
 * - GET /groups/{groupId}/settlements/{userId} - Get user's settlements
 */
@Slf4j
@RestController
@RequestMapping("/groups/{groupId}")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final SettlementService settlementService;

    /**
     * Get all balances for a group.
     * 
     * @param groupId UUID of the group
     * @return BalanceDetailResponse containing all member balances
     */
    @GetMapping("/balances")
    public ResponseEntity<BalanceDetailResponse> getGroupBalances(@PathVariable UUID groupId) {
        log.info("Fetching balances for group: {}", groupId);
        BalanceDetailResponse response = balanceService.getGroupBalances(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get balance for a specific user in a group.
     * 
     * @param groupId UUID of the group
     * @param userId  UUID of the user
     * @return BalanceResponse containing user's balance
     */
    @GetMapping("/balances/{userId}")
    public ResponseEntity<BalanceResponse> getUserBalance(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        log.info("Fetching balance for user {} in group {}", userId, groupId);
        BalanceResponse response = balanceService.getUserBalance(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get optimized settlement plan for a group.
     * 
     * @param groupId UUID of the group
     * @return SettlementDetailResponse containing all settlement transactions
     */
    @GetMapping("/settlements")
    public ResponseEntity<SettlementDetailResponse> getGroupSettlements(@PathVariable UUID groupId) {
        log.info("Fetching settlements for group: {}", groupId);
        SettlementDetailResponse response = settlementService.getGroupSettlements(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get settlement transactions for a specific user in a group.
     * 
     * @param groupId UUID of the group
     * @param userId  UUID of the user
     * @return SettlementDetailResponse containing user's settlement transactions
     */
    @GetMapping("/settlements/{userId}")
    public ResponseEntity<SettlementDetailResponse> getUserSettlements(
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        log.info("Fetching settlements for user {} in group {}", userId, groupId);
        SettlementDetailResponse response = settlementService.getUserSettlements(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
