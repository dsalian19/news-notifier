package com.newsnotifier.service;

import com.newsnotifier.dto.CategoryResponse;
import com.newsnotifier.repository.CategoryRepository;
import com.newsnotifier.repository.UserCategoryRepository;
import com.newsnotifier.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

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
}
