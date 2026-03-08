package com.smartsplit.group.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequest(
    @NotBlank(message = "Group name cannot be empty")
    @Size(min = 3, max = 50, message = "Group name must be between 3 and 50 characters")
    String name
) {}