package com.smartsplit.expense.controller;

import com.smartsplit.expense.dto.ExpenseRequest;
import com.smartsplit.expense.dto.ExpenseResponse;
import com.smartsplit.expense.service.ExpenseService;
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
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/expenses")
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/groups/{groupId}/expenses")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(@PathVariable UUID groupId) {
        List<ExpenseResponse> responses = expenseService.getExpensesByGroupId(groupId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            @PathVariable UUID expenseId,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(expenseId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<Void> deleteExpense(@PathVariable UUID expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.noContent().build();
    }
}
