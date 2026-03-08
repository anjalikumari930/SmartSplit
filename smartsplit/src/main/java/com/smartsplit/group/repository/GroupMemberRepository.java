package com.smartsplit.group.repository;

import com.smartsplit.group.GroupMember;
import com.smartsplit.group.Group;
import com.smartsplit.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    List<GroupMember> findByUser(User user);
    boolean existsByUserAndGroup(User user, Group group);
}
