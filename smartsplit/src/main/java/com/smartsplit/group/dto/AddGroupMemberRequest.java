package com.smartsplit.group.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddGroupMemberRequest(
    @NotNull(message = "User ID cannot be null")
    UUID userId
) {}