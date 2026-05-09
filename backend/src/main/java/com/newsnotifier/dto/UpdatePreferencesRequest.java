package com.newsnotifier.dto;

public record UpdatePreferencesRequest(boolean notifyEmail, boolean notifySms) {}
