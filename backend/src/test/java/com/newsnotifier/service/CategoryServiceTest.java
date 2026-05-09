package com.newsnotifier.service;

import com.newsnotifier.model.Category;
import com.newsnotifier.model.User;
import com.newsnotifier.model.UserCategory;
import com.newsnotifier.repository.CategoryRepository;
import com.newsnotifier.repository.UserCategoryRepository;
import com.newsnotifier.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private UserCategoryRepository userCategoryRepository;
    @Mock private UserRepository userRepository;

    private CategoryService categoryService;

    private final User testUser = User.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("hash")
            .phoneNumber("555-1234")
            .build();

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, userCategoryRepository, userRepository);
    }

    @Test
    void replaceSubscriptions_success() {
        UUID catId1 = UUID.randomUUID();
        UUID catId2 = UUID.randomUUID();
        Category cat1 = Category.builder().id(catId1).name("Technology").guardianKey("technology").build();
        Category cat2 = Category.builder().id(catId2).name("Science").guardianKey("science").build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAllById(List.of(catId1, catId2))).thenReturn(List.of(cat1, cat2));
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of());

        categoryService.replaceSubscriptions("test@example.com", List.of(catId1, catId2));

        verify(userCategoryRepository).deleteAll(any());
        verify(userCategoryRepository).saveAll(argThat(list -> {
            var saved = (List<UserCategory>) list;
            return saved.size() == 2;
        }));
    }

    @Test
    void replaceSubscriptions_invalidCategoryId_throws404() {
        UUID validId = UUID.randomUUID();
        UUID invalidId = UUID.randomUUID();

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAllById(List.of(validId, invalidId))).thenReturn(List.of(
                Category.builder().id(validId).name("Technology").guardianKey("technology").build()
        ));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.replaceSubscriptions("test@example.com", List.of(validId, invalidId)));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(userCategoryRepository, never()).deleteAll(any());
        verify(userCategoryRepository, never()).saveAll(any());
    }

    @Test
    void replaceSubscriptions_userNotFound_throws404() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categoryService.replaceSubscriptions("nobody@example.com", List.of(UUID.randomUUID())));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        verify(categoryRepository, never()).findAllById(any());
    }

    @Test
    void replaceSubscriptions_emptyList_clearsAllSubscriptions() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findAllById(List.of())).thenReturn(List.of());
        when(userCategoryRepository.findByUser(testUser)).thenReturn(List.of());

        categoryService.replaceSubscriptions("test@example.com", List.of());

        verify(userCategoryRepository).deleteAll(any());
        verify(userCategoryRepository).saveAll(argThat(list -> ((List<?>) list).isEmpty()));
    }
}
