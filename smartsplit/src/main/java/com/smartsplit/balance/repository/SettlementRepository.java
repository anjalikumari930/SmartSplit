package com.smartsplit.balance.repository;

import com.smartsplit.balance.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findByIdAndIsSettledFalse(UUID settlementId);
}
