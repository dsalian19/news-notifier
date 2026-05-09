package com.newsnotifier.controller;

import com.newsnotifier.dto.UserProfileResponse;
import com.newsnotifier.service.CategoryService;
import com.newsnotifier.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

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
}
