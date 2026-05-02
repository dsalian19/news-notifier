package com.newsnotifier.repository;

import com.newsnotifier.model.Category;
import com.newsnotifier.model.CategoryDigest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDigestRepository extends JpaRepository<CategoryDigest, UUID> {
    Optional<CategoryDigest> findByCategoryAndDigestDate(Category category, LocalDate digestDate);
    List<CategoryDigest> findByCategoryIn(List<Category> categories);
}
