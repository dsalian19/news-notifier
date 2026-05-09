package com.newsnotifier.dto;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String phoneNumber, boolean notifyEmail, boolean notifySms) {}
