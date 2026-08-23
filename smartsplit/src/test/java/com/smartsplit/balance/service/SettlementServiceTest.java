package com.smartsplit.balance.service;

import com.smartsplit.balance.dto.BalanceDetailResponse;
import com.smartsplit.balance.dto.BalanceResponse;
import com.smartsplit.balance.dto.SettlementDetailResponse;
import com.smartsplit.balance.dto.SettlementResponse;
import com.smartsplit.balance.entity.Settlement;
import com.smartsplit.balance.repository.SettlementRepository;
import com.smartsplit.balance.utility.SplitwiseSimplify;
import com.smartsplit.group.Group;
import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock
    private BalanceService balanceService;

    @Mock
    private SplitwiseSimplify splitwiseSimplify;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private SettlementRepository settlementRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SettlementService settlementService;

    @Test
    void getGroupSettlementsPersistsCalculatedSettlementRows() {
        UUID groupId = UUID.randomUUID();
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();

        Group group = new Group();
        group.setId(groupId);

        User alice = new User();
        alice.setId(aliceId);
        alice.setName("Alice");

        User bob = new User();
        bob.setId(bobId);
        bob.setName("Bob");

        GroupMember aliceMember = new GroupMember();
        aliceMember.setUser(alice);

        GroupMember bobMember = new GroupMember();
        bobMember.setUser(bob);

        when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
        when(groupMemberRepository.findByGroupId(groupId)).thenReturn(List.of(aliceMember, bobMember));
        when(balanceService.getGroupBalances(groupId)).thenReturn(new BalanceDetailResponse(
                groupId,
                List.of(
                        new BalanceResponse(aliceId, "Alice", new BigDecimal("-10.00")),
                        new BalanceResponse(bobId, "Bob", new BigDecimal("10.00")))));
        when(splitwiseSimplify.calculateSettlements(any(), any())).thenReturn(List.of(
                new SettlementResponse(bobId, "Bob", aliceId, "Alice", new BigDecimal("10.00"))));
        when(settlementRepository.save(any(Settlement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SettlementDetailResponse response = settlementService.getGroupSettlements(groupId);

        assertEquals(1, response.settlements().size());
        verify(settlementRepository, times(1)).save(any(Settlement.class));
    }

    @Test
    void getUserSettlementsIsNotReadOnlyBecauseItMayPersistSettlements() throws NoSuchMethodException {
        Method method = SettlementService.class.getMethod("getUserSettlements", UUID.class, UUID.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertNotNull(transactional);
        assertFalse(transactional.readOnly());
    }
}
