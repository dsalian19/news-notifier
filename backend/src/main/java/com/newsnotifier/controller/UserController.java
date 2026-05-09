package com.newsnotifier.controller;

import com.newsnotifier.dto.CategoryResponse;
import com.newsnotifier.dto.UpdateCategoriesRequest;
import com.newsnotifier.dto.UpdatePreferencesRequest;
import com.newsnotifier.dto.UserProfileResponse;
import com.newsnotifier.service.CategoryService;
import com.newsnotifier.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CategoryService categoryService;

    @GetMapping("/me")
    public UserProfileResponse getMe(Principal principal) {
        return userService.getProfile(principal.getName());
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getMyCategories(Principal principal) {
        return categoryService.getUserCategories(principal.getName());
    }

    @PutMapping("/categories")
    public void replaceSubscriptions(@RequestBody UpdateCategoriesRequest request, Principal principal) {
        categoryService.replaceSubscriptions(principal.getName(), request.categoryIds());
    }

    @PutMapping("/preferences")
    public UserProfileResponse updatePreferences(@RequestBody UpdatePreferencesRequest request, Principal principal) {
        return userService.updatePreferences(principal.getName(), request);
    }
}
