package com.smartsplit.balance.controller;

import com.smartsplit.balance.dto.BalanceDetailResponse;
import com.smartsplit.balance.dto.BalanceResponse;
import com.smartsplit.balance.dto.SettlementDetailResponse;
import com.smartsplit.balance.service.BalanceService;
import com.smartsplit.balance.service.SettlementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Balance & Settlement", description = "Balance and settlement calculation endpoints")
public class BalanceController {

    private final BalanceService balanceService;
    private final SettlementService settlementService;

    @GetMapping("/balances")
    @Operation(summary = "Get all balances in a group", description = "Retrieve balance information for all members in a group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved group balances")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<BalanceDetailResponse> getGroupBalances(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        log.info("Fetching balances for group: {}", groupId);
        BalanceDetailResponse response = balanceService.getGroupBalances(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/balances/{userId}")
    @Operation(summary = "Get user balance in group", description = "Retrieve balance information for a specific user in a group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user balance")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group or user not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<BalanceResponse> getUserBalance(
            @PathVariable @Parameter(description = "Group ID") UUID groupId,
            @PathVariable @Parameter(description = "User ID") UUID userId) {
        log.info("Fetching balance for user {} in group {}", userId, groupId);
        BalanceResponse response = balanceService.getUserBalance(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/settlements")
    @Operation(summary = "Get optimized settlement plan", description = "Retrieve the optimized settlement plan for all members in a group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved settlement plan")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SettlementDetailResponse> getGroupSettlements(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        log.info("Fetching settlements for group: {}", groupId);
        SettlementDetailResponse response = settlementService.getGroupSettlements(groupId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/settlements/{userId}")
    @Operation(summary = "Get user settlements", description = "Retrieve settlement transactions for a specific user in a group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user settlements")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group or user not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<SettlementDetailResponse> getUserSettlements(
            @PathVariable @Parameter(description = "Group ID") UUID groupId,
            @PathVariable @Parameter(description = "User ID") UUID userId) {
        log.info("Fetching settlements for user {} in group {}", userId, groupId);
        SettlementDetailResponse response = settlementService.getUserSettlements(groupId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
