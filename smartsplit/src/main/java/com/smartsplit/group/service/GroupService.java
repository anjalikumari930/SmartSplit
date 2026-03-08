package com.smartsplit.group.service;

import com.smartsplit.group.Group;
import com.smartsplit.group.GroupMember;
import com.smartsplit.group.dto.AddGroupMemberRequest;
import com.smartsplit.group.dto.CreateGroupRequest;
import com.smartsplit.group.dto.GroupMemberResponse;
import com.smartsplit.group.dto.GroupResponse;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.group.repository.GroupRepository;
import com.smartsplit.user.User;
import com.smartsplit.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request) {
        User currentUser = getCurrentUser();

        Group group = new Group();
        group.setName(request.name());
        group.setCreatedAt(LocalDateTime.now());
        group.setCreatedBy(currentUser);

        Group savedGroup = groupRepository.save(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(savedGroup);
        groupMember.setUser(currentUser);
        groupMember.setRole("ADMIN");
        groupMemberRepository.save(groupMember);

        return new GroupResponse(
                savedGroup.getId(),
                savedGroup.getName(),
                savedGroup.getCreatedBy().getId(),
                savedGroup.getCreatedAt()
        );
    }

    @Transactional
    public void addMember(UUID groupId, AddGroupMemberRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.userId()));

        if (groupMemberRepository.existsByUserAndGroup(user, group)) {
            throw new IllegalStateException("User is already a member of this group.");
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole("MEMBER");

        groupMemberRepository.save(groupMember);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getUserGroups() {
        User currentUser = getCurrentUser();
        List<GroupMember> groupMembers = groupMemberRepository.findByUser(currentUser);
        return groupMembers.stream()
                .map(GroupMember::getGroup)
                .map(group -> new GroupResponse(
                        group.getId(),
                        group.getName(),
                        group.getCreatedBy().getId(),
                        group.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public GroupResponse getGroupDetails(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
        return new GroupResponse(
                group.getId(),
                group.getName(),
                group.getCreatedBy().getId(),
                group.getCreatedAt()
        );
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<GroupMemberResponse> getGroupMembers(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findAll().stream()
                .filter(gm -> gm.getGroup().getId().equals(groupId))
                .collect(Collectors.toList());
        return members.stream()
                .map(gm -> new GroupMemberResponse(
                        gm.getUser().getId(),
                        gm.getUser().getName(),
                        gm.getUser().getEmail(),
                        gm.getRole()
                ))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void removeMemberFromGroup(UUID groupId, UUID memberId) {
        GroupMember groupMember = groupMemberRepository.findAll().stream()
                .filter(gm -> gm.getGroup().getId().equals(groupId) && gm.getUser().getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Member not found in group"));
        groupMemberRepository.delete(groupMember);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void leaveGroup(UUID groupId) {
        User currentUser = getCurrentUser();
        GroupMember groupMember = groupMemberRepository.findAll().stream()
                .filter(gm -> gm.getGroup().getId().equals(groupId) && gm.getUser().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));
        groupMemberRepository.delete(groupMember);
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void deleteGroup(UUID groupId) {
        List<GroupMember> members = groupMemberRepository.findAll().stream()
                .filter(gm -> gm.getGroup().getId().equals(groupId))
                .collect(Collectors.toList());
        groupMemberRepository.deleteAll(members);
        groupRepository.deleteById(groupId);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + username));
    }

    private static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }
}
