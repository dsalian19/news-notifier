package com.newsnotifier.service;

import com.newsnotifier.model.Category;
import com.newsnotifier.model.CategoryDigest;
import com.newsnotifier.model.User;
import com.newsnotifier.model.UserCategory;
import com.newsnotifier.repository.CategoryDigestRepository;
import com.newsnotifier.repository.UserCategoryRepository;
import com.newsnotifier.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigestServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserCategoryRepository userCategoryRepository;
    @Mock private CategoryDigestRepository categoryDigestRepository;

    private DigestService digestService;

    private final User testUser = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("hash")
            .phoneNumber("555-1234")
            .build();

    private final Category techCategory = Category.builder()
            .id(UUID.randomUUID())
            .name("Technology")
            .guardianKey("technology")
            .build();

    private final Category scienceCategory = Category.builder()
            .id(UUID.randomUUID())
            .name("Science")
            .guardianKey("science")
            .build();

    private UserCategory techSubscription;
    private CategoryDigest techDigest;

    @BeforeEach
    void setUp() {
        digestService = new DigestService(userRepository, userCategoryRepository, categoryDigestRepository);

        techSubscription = UserCategory.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .category(techCategory)
                .build();

        techDigest = CategoryDigest.builder()
                .id(UUID.randomUUID())
                .category(techCategory)
                .digestDate(LocalDate.of(2025, 5, 10))
                .shortSummary("Tech news today")
                .longSummary("A longer summary of tech news")
                .articleUrls(List.of("https://theguardian.com/tech/1"))
                .build();
    }

    @Test
    void getDigests_noFilters_returnsAllSubscribedDigests() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));
        when(categoryDigestRepository.findForUserCategories(List.of(techCategory), null, null, null))
                .thenReturn(List.of(techDigest));

        var results = digestService.getDigests("test@example.com", null, null, null);

        assertEquals(1, results.size());
        assertEquals(techDigest.getId(), results.get(0).id());
        assertEquals(techCategory.getId(), results.get(0).categoryId());
        assertEquals("Technology", results.get(0).categoryName());
    }

    @Test
    void getDigests_withCategoryFilter_returnsFilteredDigests() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));
        when(categoryDigestRepository.findForUserCategories(List.of(techCategory), techCategory.getId(), null, null))
                .thenReturn(List.of(techDigest));

        var results = digestService.getDigests("test@example.com", techCategory.getId(), null, null);

        assertEquals(1, results.size());
        assertEquals(techCategory.getId(), results.get(0).categoryId());
    }

    @Test
    void getDigests_withCategoryNotSubscribed_throwsNotFound() {
        UUID unsubscribedId = UUID.randomUUID();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> digestService.getDigests("test@example.com", unsubscribedId, null, null));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(categoryDigestRepository, never()).findForUserCategories(any(), any(), any(), any());
    }

    @Test
    void getDigests_withDateFilter_returnsFilteredDigests() {
        LocalDate date = LocalDate.of(2025, 5, 10);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));
        when(categoryDigestRepository.findForUserCategories(List.of(techCategory), null, date, null))
                .thenReturn(List.of(techDigest));

        var results = digestService.getDigests("test@example.com", null, date, null);

        assertEquals(1, results.size());
        assertEquals(date, results.get(0).digestDate());
    }

    @Test
    void getDigests_withKeywordFilter_passesKeywordToRepository() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));
        when(categoryDigestRepository.findForUserCategories(List.of(techCategory), null, null, "AI"))
                .thenReturn(List.of(techDigest));

        var results = digestService.getDigests("test@example.com", null, null, "AI");

        assertEquals(1, results.size());
        verify(categoryDigestRepository).findForUserCategories(List.of(techCategory), null, null, "AI");
    }

    @Test
    void getDigests_emptySubscriptions_returnsEmptyList() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of());

        var results = digestService.getDigests("test@example.com", null, null, null);

        assertTrue(results.isEmpty());
        verify(categoryDigestRepository, never()).findForUserCategories(any(), any(), any(), any());
    }

    @Test
    void getDigestById_happyPath_returnsFullDetail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryDigestRepository.findById(techDigest.getId())).thenReturn(Optional.of(techDigest));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));

        var result = digestService.getDigestById("test@example.com", techDigest.getId());

        assertEquals(techDigest.getId(), result.id());
        assertEquals(techCategory.getId(), result.categoryId());
        assertEquals("A longer summary of tech news", result.longSummary());
        assertEquals(List.of("https://theguardian.com/tech/1"), result.articleUrls());
    }

    @Test
    void getDigestById_digestNotFound_throwsNotFound() {
        UUID missingId = UUID.randomUUID();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryDigestRepository.findById(missingId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> digestService.getDigestById("test@example.com", missingId));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getDigestById_categoryNotSubscribed_throwsNotFound() {
        CategoryDigest scienceDigest = CategoryDigest.builder()
                .id(UUID.randomUUID())
                .category(scienceCategory)
                .digestDate(LocalDate.of(2025, 5, 10))
                .shortSummary("Science news")
                .longSummary("A longer summary of science news")
                .articleUrls(List.of())
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryDigestRepository.findById(scienceDigest.getId())).thenReturn(Optional.of(scienceDigest));
        // user is only subscribed to tech, not science
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of(techSubscription));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> digestService.getDigestById("test@example.com", scienceDigest.getId()));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
}
