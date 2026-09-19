package com.smartsplit.auth.dto;

/**
 * Authentication response DTO containing the JWT token.
 */
public record AuthResponse(
    String token
) {}
