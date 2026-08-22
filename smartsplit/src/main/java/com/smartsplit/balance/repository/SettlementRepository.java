package com.smartsplit.balance.repository;

import com.smartsplit.balance.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    Optional<Settlement> findByIdAndIsSettledFalse(UUID settlementId);

    @Modifying
    @Query("delete from Settlement s where s.group.id = :groupId and s.isSettled = false")
    void deleteUnsettledByGroupId(@Param("groupId") UUID groupId);
}
