package com.smartsplit.notification.listener;

import com.smartsplit.group.GroupMember;
import com.smartsplit.group.repository.GroupMemberRepository;
import com.smartsplit.notification.entity.NotificationType;
import com.smartsplit.notification.event.ExpenseCreatedEvent;
import com.smartsplit.notification.event.ExpenseUpdatedEvent;
import com.smartsplit.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExpenseNotificationListener {

    private final NotificationService notificationService;
    private final GroupMemberRepository groupMemberRepository;

    @EventListener
    @Async
    public void onExpenseCreated(ExpenseCreatedEvent event) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(event.getGroupId());

        for (GroupMember member : groupMembers) {
            // Don't send notification to the person who created the expense
            if (!member.getUser().getId().equals(event.getPaidByUserId())) {
                String title = "New Expense Added";
                String message = String.format(
                        "%s added a new expense: %s for $%.2f in the group.",
                        event.getPaidByUserName(),
                        event.getDescription(),
                        event.getAmount());

                notificationService.createNotification(
                        member.getUser().getId(),
                        title,
                        message,
                        NotificationType.IN_APP);
            }
        }
    }

    @EventListener
    @Async
    public void onExpenseUpdated(ExpenseUpdatedEvent event) {
        List<GroupMember> groupMembers = groupMemberRepository.findByGroupId(event.getGroupId());

        for (GroupMember member : groupMembers) {
            // Don't send notification to the person who updated the expense
            if (!member.getUser().getId().equals(event.getPaidByUserId())) {
                String title = "Expense Updated";
                String message = String.format(
                        "%s updated an expense: %s for $%.2f in the group.",
                        event.getPaidByUserName(),
                        event.getDescription(),
                        event.getAmount());

                notificationService.createNotification(
                        member.getUser().getId(),
                        title,
                        message,
                        NotificationType.IN_APP);
            }
        }
    }
}
