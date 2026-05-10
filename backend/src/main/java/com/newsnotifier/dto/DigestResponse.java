package com.newsnotifier.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DigestResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        LocalDate digestDate,
        String shortSummary,
        String longSummary,
        List<String> articleUrls
) {}
