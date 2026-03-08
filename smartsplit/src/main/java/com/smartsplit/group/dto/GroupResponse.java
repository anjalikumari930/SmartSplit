package com.smartsplit.group.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupResponse(
    UUID id,
    String name,
    UUID createdBy,
    LocalDateTime createdAt
) {}