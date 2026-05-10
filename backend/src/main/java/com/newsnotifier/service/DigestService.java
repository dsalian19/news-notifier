package com.newsnotifier.service;

import com.newsnotifier.dto.DigestResponse;
import com.newsnotifier.model.CategoryDigest;
import com.newsnotifier.repository.CategoryDigestRepository;
import com.newsnotifier.repository.UserCategoryRepository;
import com.newsnotifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DigestService {

    private final UserRepository userRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final CategoryDigestRepository categoryDigestRepository;

    public List<DigestResponse> getDigests(String email, UUID categoryId, LocalDate date, String keyword) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var subscriptions = userCategoryRepository.findByUser(user);
        if (subscriptions.isEmpty()) {
            return List.of();
        }

        var subscribedCategoryIds = subscriptions.stream()
                .map(uc -> uc.getCategory().getId())
                .collect(Collectors.toSet());

        if (categoryId != null && !subscribedCategoryIds.contains(categoryId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }

        var categories = subscriptions.stream()
                .map(uc -> uc.getCategory())
                .toList();

        return categoryDigestRepository.findForUserCategories(categories, categoryId, date, keyword)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public DigestResponse getDigestById(String email, UUID id) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var digest = categoryDigestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Digest not found"));

        var subscribedCategoryIds = userCategoryRepository.findByUser(user).stream()
                .map(uc -> uc.getCategory().getId())
                .collect(Collectors.toSet());

        if (!subscribedCategoryIds.contains(digest.getCategory().getId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Digest not found");
        }

        return toResponse(digest);
    }

    private DigestResponse toResponse(CategoryDigest d) {
        return new DigestResponse(
                d.getId(),
                d.getCategory().getId(),
                d.getCategory().getName(),
                d.getDigestDate(),
                d.getShortSummary(),
                d.getLongSummary(),
                d.getArticleUrls()
        );
    }
}
