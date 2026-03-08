package com.smartsplit.group.dto;

import java.util.UUID;

public record GroupMemberResponse(
    UUID id,
    String name,
    String email,
    String role
) {}