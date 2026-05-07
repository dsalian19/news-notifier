package com.newsnotifier.dto;

import java.util.UUID;

public record AuthResponse(String token, UUID id, String email, boolean notifyEmail, boolean notifySms) {}
