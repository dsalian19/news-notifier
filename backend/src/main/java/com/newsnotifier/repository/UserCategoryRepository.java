package com.newsnotifier.repository;

import com.newsnotifier.model.Category;
import com.newsnotifier.model.User;
import com.newsnotifier.model.UserCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserCategoryRepository extends JpaRepository<UserCategory, UUID> {
    List<UserCategory> findByUser(User user);
    Optional<UserCategory> findByUserAndCategory(User user, Category category);
}
