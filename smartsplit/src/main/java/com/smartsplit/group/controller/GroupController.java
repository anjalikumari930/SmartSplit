package com.smartsplit.group.controller;

import com.smartsplit.group.dto.AddGroupMemberRequest;
import com.smartsplit.group.dto.CreateGroupRequest;
import com.smartsplit.group.dto.GroupResponse;
import com.smartsplit.group.dto.GroupMemberResponse;
import com.smartsplit.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getUserGroups() {
        List<GroupResponse> groups = groupService.getUserGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<Void> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        groupService.addMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupDetails(@PathVariable UUID groupId) {
        GroupResponse response = groupService.getGroupDetails(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable UUID groupId) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    public ResponseEntity<Void> removeMember(@PathVariable UUID groupId, @PathVariable UUID memberId) {
        groupService.removeMemberFromGroup(groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable UUID groupId) {
        groupService.leaveGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable UUID groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }
}