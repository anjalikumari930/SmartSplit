package com.smartsplit.notification.controller;

import com.smartsplit.exception.ResourceNotFoundException;
import com.smartsplit.notification.dto.NotificationResponse;
import com.smartsplit.notification.service.NotificationService;
import com.smartsplit.user.User;
import com.smartsplit.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get user notifications")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<Page<NotificationResponse>> getUserNotifications(
            Authentication authentication,
            @Parameter(description = "Page number", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10") @RequestParam(defaultValue = "10") int size) {

        UUID userId = extractUserIdFromAuth(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponse> notifications = notificationService.getUserNotifications(userId, pageable);

        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            Authentication authentication) {

        UUID userId = extractUserIdFromAuth(authentication);
        notificationService.markAsRead(notificationId, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(Authentication authentication) {
        UUID userId = extractUserIdFromAuth(authentication);
        notificationService.markAllAsRead(userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    private UUID extractUserIdFromAuth(Authentication authentication) {
        // Extract email from authentication principal
        Object principal = authentication.getPrincipal();

        String extractedEmail = null;
        if (principal instanceof UserDetails) {
            extractedEmail = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            extractedEmail = (String) principal;
        }

        if (extractedEmail == null) {
            throw new ResourceNotFoundException("Cannot extract user information from authentication context");
        }

        // Make final copy for lambda usage
        final String emailForLambda = extractedEmail;

        // Fetch user by email to get UUID
        User user = userRepository.findByEmail(emailForLambda)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + emailForLambda));

        return user.getId();
    }
}
