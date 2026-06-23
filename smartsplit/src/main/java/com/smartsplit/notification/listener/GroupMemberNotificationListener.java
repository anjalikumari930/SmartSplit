package com.smartsplit.notification.listener;

import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.event.GroupMemberAddedEvent;
import com.smartsplit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroupMemberNotificationListener {

    private final NotificationService notificationService;
    private final GroupMemberRepository groupMemberRepository;

    @EventListener
    @Async
    public void onGroupMemberAdded(GroupMemberAddedEvent event) {
        // Notify the newly added member
        String newMemberTitle = "Welcome to Group";
        String newMemberMessage = String.format(
                "You have been added to the group '%s' by %s.",
                event.getGroupName(),
                event.getAddedByName());

        notificationService.createNotification(
                event.getNewMemberId(),
                newMemberTitle,
                newMemberMessage,
                NotificationType.IN_APP);

        // Notify existing group members about the new member
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(event.getGroupId());

        for (GroupMember member : groupMembers) {
            // Don't send notification to the newly added member or the person who added
            // them
            if (!member.getUser().getId().equals(event.getNewMemberId()) &&
                    !member.getUser().getId().equals(event.getNewMemberId())) {
                String title = "New Member Added";
                String message = String.format(
                        "%s has been added to the group '%s'.",
                        event.getNewMemberName(),
                        event.getGroupName());

                notificationService.createNotification(
                        member.getUser().getId(),
                        title,
                        message,
                        NotificationType.IN_APP);
            }
        }
    }
}
