package com.smartsplit.expense.service;

import com.smartsplit.expense.dto.ExpenseRequest;
import com.smartsplit.expense.dto.ExpenseResponse;
import com.smartsplit.expense.dto.SplitRequest;
import com.smartsplit.expense.dto.SplitResponse;
import com.smartsplit.expense.entity.Expense;
import com.smartsplit.expense.entity.Split;
import com.smartsplit.expense.repository.ExpenseRepository;
import com.smartsplit.expense.strategy.SplitStrategyFactory;
import com.smartsplit.exception.BadRequestException;
import com.smartsplit.exception.ResourceNotFoundException;
import com.smartsplit.group.Group;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.notification.event.ExpenseCreatedEvent;
import com.smartsplit.notification.event.ExpenseUpdatedEvent;
import com.smartsplit.user.User;
import com.smartsplit.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SplitStrategyFactory splitStrategyFactory;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        validateBasicRequest(request);

        User paidBy = findUser(request.paidByUserId());
        Group group = findGroup(request.groupId());
        ensureGroupMembership(paidBy, group, "Paid by user must belong to the selected group.");

        Map<UUID, User> splitUsers = resolveSplitUsers(request.splits(), group);
        Expense expense = buildExpense(request, paidBy, group);

        List<Split> splits = splitStrategyFactory
                .getStrategy(request.splitType())
                .calculateSplits(expense.getAmount(), request.splits(), splitUsers);

        attachSplitsToExpense(expense, splits);

        Expense savedExpense = expenseRepository.save(expense);

        // Publish event
        ExpenseCreatedEvent event = new ExpenseCreatedEvent(
                this,
                savedExpense.getId(),
                savedExpense.getDescription(),
                savedExpense.getAmount(),
                savedExpense.getPaidBy().getId(),
                savedExpense.getGroup().getId(),
                savedExpense.getPaidBy().getName());
        eventPublisher.publishEvent(event);

        return mapToResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroupId(UUID groupId) {
        findGroup(groupId);
        return expenseRepository.findByGroupId(groupId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponse updateExpense(UUID expenseId, ExpenseRequest request) {
        validateBasicRequest(request);

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));

        User paidBy = findUser(request.paidByUserId());
        Group group = findGroup(request.groupId());
        ensureGroupMembership(paidBy, group, "Paid by user must belong to the selected group.");

        Map<UUID, User> splitUsers = resolveSplitUsers(request.splits(), group);
        expense.setDescription(request.description());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setSplitType(request.splitType());

        expense.getSplits().clear();
        List<Split> splits = splitStrategyFactory
                .getStrategy(request.splitType())
                .calculateSplits(expense.getAmount(), request.splits(), splitUsers);
        attachSplitsToExpense(expense, splits);

        Expense updatedExpense = expenseRepository.save(expense);

        // Publish event
        ExpenseUpdatedEvent event = new ExpenseUpdatedEvent(
                this,
                updatedExpense.getId(),
                updatedExpense.getDescription(),
                updatedExpense.getAmount(),
                updatedExpense.getPaidBy().getId(),
                updatedExpense.getGroup().getId(),
                updatedExpense.getPaidBy().getName());
        eventPublisher.publishEvent(event);

        return mapToResponse(updatedExpense);
    }

    @Transactional
    public void deleteExpense(UUID expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + expenseId));
        expenseRepository.delete(expense);
    }

    private void validateBasicRequest(ExpenseRequest request) {
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be greater than zero.");
        }
        if (request.splits() == null || request.splits().isEmpty()) {
            throw new BadRequestException("At least one split is required.");
        }
    }

    private User findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Group findGroup(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
    }

    private void ensureGroupMembership(User user, Group group, String message) {
        if (!groupMemberRepository.existsByUserAndGroup(user, group)) {
            throw new BadRequestException(message);
        }
    }

    private Map<UUID, User> resolveSplitUsers(List<SplitRequest> splitRequests, Group group) {
        Map<UUID, User> userMap = new HashMap<>();
        for (SplitRequest request : splitRequests) {
            if (userMap.containsKey(request.userId())) {
                throw new BadRequestException("Duplicate split user is not allowed: " + request.userId());
            }
            User user = findUser(request.userId());
            ensureGroupMembership(user, group, "All split users must belong to the selected group.");
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    private Expense buildExpense(ExpenseRequest request, User paidBy, Group group) {
        Expense expense = new Expense();
        expense.setDescription(request.description());
        expense.setAmount(request.amount().setScale(2, RoundingMode.HALF_UP));
        expense.setPaidBy(paidBy);
        expense.setGroup(group);
        expense.setSplitType(request.splitType());
        expense.setCreatedAt(LocalDateTime.now());
        return expense;
    }

    private void attachSplitsToExpense(Expense expense, List<Split> splits) {
        for (Split split : splits) {
            split.setExpense(expense);
            expense.getSplits().add(split);
        }
    }

    private ExpenseResponse mapToResponse(Expense expense) {
        List<SplitResponse> splits = expense.getSplits().stream()
                .map(split -> new SplitResponse(split.getUser().getId(), split.getAmountOwed()))
                .collect(Collectors.toList());

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getPaidBy().getId(),
                expense.getGroup().getId(),
                expense.getSplitType(),
                splits,
                expense.getCreatedAt());
    }
}
