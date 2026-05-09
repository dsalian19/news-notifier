package com.newsnotifier.service;

import com.newsnotifier.dto.CategoryResponse;
import com.newsnotifier.model.UserCategory;
import com.newsnotifier.repository.CategoryRepository;
import com.newsnotifier.repository.UserCategoryRepository;
import com.newsnotifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserCategoryRepository userCategoryRepository;
    private final UserRepository userRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    public List<CategoryResponse> getUserCategories(String email) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userCategoryRepository.findByUser(user).stream()
                .map(uc -> new CategoryResponse(uc.getCategory().getId(), uc.getCategory().getName()))
                .toList();
    }

    public void replaceSubscriptions(String email, List<UUID> categoryIds) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        var categories = categoryRepository.findAllById(categoryIds);
        if (categories.size() != categoryIds.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "One or more categories not found");
        }
        userCategoryRepository.deleteAll(userCategoryRepository.findByUser(user));
        var newSubscriptions = categories.stream()
                .map(c -> UserCategory.builder().user(user).category(c).build())
                .toList();
        userCategoryRepository.saveAll(newSubscriptions);
    }
}
