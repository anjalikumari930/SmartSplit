package com.smartsplit.balance.repository;

import com.smartsplit.expense.repository.ExpenseRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for balance-related operations.
 * Currently a marker interface extending ExpenseRepository.
 * Can be extended in future for persisting balance snapshots.
 */
@Repository
public interface BalanceRepository extends ExpenseRepository {
}
