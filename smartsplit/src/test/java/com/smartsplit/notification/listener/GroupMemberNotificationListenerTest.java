package com.smartsplit.notification.listener;

import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.event.GroupMemberAddedEvent;
import com.smartsplit.notification.service.NotificationService;
import com.smartsplit.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupMemberNotificationListenerTest {

    @Mock
    private NotificationService notificationService;
    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private GroupMemberNotificationListener listener;

    private User addedByUser;
    private User newMemberUser;
    private User existingMemberUser;
    private GroupMemberAddedEvent event;

    @BeforeEach
    void setUp() {
        // --- GIVEN ---
        // Create user objects for our test scenario
        addedByUser = new User();
        addedByUser.setId(UUID.randomUUID());
        addedByUser.setName("Admin User");

        newMemberUser = new User();
        newMemberUser.setId(UUID.randomUUID());
        newMemberUser.setName("New Member");

        existingMemberUser = new User();
        existingMemberUser.setId(UUID.randomUUID());
        existingMemberUser.setName("Existing Member");

        // Create the event that the listener will process.
        // NOTE: The event needs the ID of the user who performed the action.
        // We'll assume the GroupMemberAddedEvent is updated to include `addedByUserId`.
        event = new GroupMemberAddedEvent(
                this,
                UUID.randomUUID(), // new group member ID
                newMemberUser.getId(), // new member user ID
                addedByUser.getId(), // user ID of the person who added the member
                UUID.randomUUID(), // group ID
                "Test Group", // group name
                newMemberUser.getName(), // new member name
                addedByUser.getName() // name of person who added member
        );
    }

    @Test
    void shouldSendWelcomeNotificationToNewMember() {
        // --- WHEN ---
        // Call the method we are testing
        listener.onGroupMemberAdded(event);

        // --- THEN ---
        // Verify that a notification was created specifically for the new member.
        verify(notificationService).createNotification(
                eq(newMemberUser.getId()),
                eq("Welcome to Group"),
                any(String.class),
                eq(NotificationType.IN_APP));
    }

    @Test
    void shouldNotifyOnlyExistingMembersAndNotTheAdderOrTheNewMember() {
        // --- GIVEN ---
        // Set up the group members that the repository will return.
        // This list includes all three: the one who added, the new one, and an existing
        // one.
        GroupMember addedByGroupMember = new GroupMember();
        addedByGroupMember.setUser(addedByUser);

        GroupMember newGroupMember = new GroupMember();
        newGroupMember.setUser(newMemberUser);

        GroupMember existingGroupMember = new GroupMember();
        existingGroupMember.setUser(existingMemberUser);

        List<GroupMember> allMembers = List.of(addedByGroupMember, newGroupMember, existingGroupMember);
        when(groupMemberRepository.findByGroupId(event.getGroupId())).thenReturn(allMembers);

        // --- WHEN ---
        // Call the method we are testing
        listener.onGroupMemberAdded(event);

        // --- THEN ---
        // Verify that the notification for existing members is sent ONLY to the
        // existing member.
        // It should NOT be sent to the new member or the user who added them.
        verify(notificationService, times(1)).createNotification(
                eq(existingMemberUser.getId()),
                eq("New Member Added"),
                any(String.class),
                eq(NotificationType.IN_APP));

        // Crucially, verify the notification was NOT sent to the other two.
        verify(notificationService, never()).createNotification(eq(newMemberUser.getId()), eq("New Member Added"),
                anyString(), any());
        verify(notificationService, never()).createNotification(eq(addedByUser.getId()), eq("New Member Added"),
                anyString(), any());
    }
}