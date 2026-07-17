package com.smartsplit.group.controller;

import com.smartsplit.group.dto.AddGroupMemberRequest;
import com.smartsplit.group.dto.CreateGroupRequest;
import com.smartsplit.group.dto.GroupResponse;
import com.smartsplit.group.dto.GroupMemberResponse;
import com.smartsplit.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Groups", description = "Group management endpoints")
public class GroupController {

    private final GroupService groupService;
    

    @PostMapping
    @Operation(summary = "Create a new group", description = "Create a new expense group")
    @ApiResponse(responseCode = "201", description = "Group created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid group data")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get user's groups", description = "Retrieve all groups the current user belongs to")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved user groups")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<GroupResponse>> getUserGroups() {
        List<GroupResponse> groups = groupService.getUserGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Add member to group", description = "Add a new member to an existing group")
    @ApiResponse(responseCode = "201", description = "Member added successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> addMember(
            @PathVariable @Parameter(description = "Group ID") UUID groupId,
            @Valid @RequestBody AddGroupMemberRequest request) {
        groupService.addMember(groupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details", description = "Retrieve detailed information about a specific group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved group details")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<GroupResponse> getGroupDetails(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        GroupResponse response = groupService.getGroupDetails(groupId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "Get group members", description = "Retrieve all members of a specific group")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved group members")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<GroupMemberResponse>> getGroupMembers(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        List<GroupMemberResponse> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{groupId}/members/{memberId}")
    @Operation(summary = "Remove member from group", description = "Remove a member from a group")
    @ApiResponse(responseCode = "200", description = "Member removed successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group or member not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> removeMember(
            @PathVariable @Parameter(description = "Group ID") UUID groupId,
            @PathVariable @Parameter(description = "Member ID") UUID memberId) {
        groupService.removeMemberFromGroup(groupId, memberId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{groupId}/leave")
    @Operation(summary = "Leave a group", description = "Remove current user from a group")
    @ApiResponse(responseCode = "200", description = "Successfully left the group")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> leaveGroup(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        groupService.leaveGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete a group", description = "Delete a group and all associated data")
    @ApiResponse(responseCode = "200", description = "Group deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Group not found")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Void> deleteGroup(@PathVariable @Parameter(description = "Group ID") UUID groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }
}