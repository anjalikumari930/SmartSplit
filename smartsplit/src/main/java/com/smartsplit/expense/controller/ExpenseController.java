package com.smartsplit.expense.controller;

import com.smartsplit.expense.dto.ExpenseRequest;
import com.smartsplit.expense.dto.ExpenseResponse;
import com.smartsplit.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Expense management endpoints")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    @Operation(summary = "Create a new expense", description = "Create a new expense and split it among group members")
    @ApiResponse(responseCode = "201", description = "Expense created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid expense data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}/expenses")
    @Operation(summary = "Get all expenses in a group", description = "Retrieve all expenses for a specific group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved expenses")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable @Parameter(description = "Group ID") UUID groupId) {
        List<ExpenseResponse> responses = expenseService.getExpensesByGroupId(groupId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/expenses/{expenseId}")
    @Operation(summary = "Update an expense", description = "Update details of an existing expense")
    @ApiResponse(responseCode = "200", description = "Expense updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid expense data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable @Parameter(description = "Expense ID") UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(expenseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/expenses/{expenseId}")
    @Operation(summary = "Delete an expense", description = "Remove an expense from the system")
    @ApiResponse(responseCode = "204", description = "Expense deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Expense not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteExpense(@PathVariable @Parameter(description = "Expense ID") UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
