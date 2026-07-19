package com.smartsplit.notification.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class GroupMemberAddedEvent extends ApplicationEvent {
    private final UUID groupMemberId;
    private final UUID newMemberId;
    private final UUID addedByUserId;
    private final UUID groupId;
    private final String groupName;
    private final String newMemberName;
    private final String addedByName;

    public GroupMemberAddedEvent(
            Object source,
            groupMemberId,
            newMemberId,
            UUID addedByUserId,
            UUID groupId,
            String groupName,
            String newMemberName,
            String addedByName) {
        super(source);
        this.groupMemberId = groupMemberId;
        this.newMemberId = newMemberId;
        this.addedByUserId = addedByUserId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.newMemberName = newMemberName;
        this.addedByName = addedByName;
    }

    public UUID getGroupMemberId() {
        return groupMemberId;
    }

    public UUID getNewMemberId() {
        return newMemberId;
    }

    public UUID getAddedByUserId() {
        return addedByUserId;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getNewMemberName() {
        return newMemberName;
    }

    public String getAddedByName() {
        return addedByName;
    }
}
