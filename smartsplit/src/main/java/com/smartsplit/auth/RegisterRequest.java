package com.smartsplit.auth;

public record RegisterRequest(String name, String email, String password) {
}