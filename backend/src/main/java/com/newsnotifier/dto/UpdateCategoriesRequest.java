package com.newsnotifier.dto;

import java.util.List;
import java.util.UUID;

public record UpdateCategoriesRequest(List<UUID> categoryIds) {}
