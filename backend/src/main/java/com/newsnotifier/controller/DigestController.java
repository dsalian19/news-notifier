package com.newsnotifier.controller;

import com.newsnotifier.dto.DigestResponse;
import com.newsnotifier.service.DigestService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/digests")
@RequiredArgsConstructor
public class DigestController {

    private final DigestService digestService;

    @GetMapping
    public List<DigestResponse> getDigests(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String keyword,
            Principal principal
    ) {
        return digestService.getDigests(principal.getName(), categoryId, date, keyword);
    }

    @GetMapping("/{id}")
    public DigestResponse getDigestById(@PathVariable UUID id, Principal principal) {
        return digestService.getDigestById(principal.getName(), id);
    }
}
