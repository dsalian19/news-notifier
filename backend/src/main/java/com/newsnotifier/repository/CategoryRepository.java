package com.newsnotifier.repository;

import com.newsnotifier.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByGuardianKey(String guardianKey);
}
