package com.smartsplit.group.repository;

import com.smartsplit.group.GroupMember;
import com.smartsplit.group.Group;
import com.smartsplit.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByUser(User user);

    List<GroupMember> findByGroupId(UUID groupId);

    boolean existsByUserAndGroup(User user, Group group);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
}
