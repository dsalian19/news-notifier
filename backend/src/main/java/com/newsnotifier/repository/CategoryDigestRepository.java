package com.newsnotifier.repository;

import com.newsnotifier.model.Category;
import com.newsnotifier.model.CategoryDigest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryDigestRepository extends JpaRepository<CategoryDigest, UUID> {
    Optional<CategoryDigest> findByCategoryAndDigestDate(Category category, LocalDate digestDate);
    List<CategoryDigest> findByCategoryIn(List<Category> categories);

    @Query("SELECT d FROM CategoryDigest d " +
           "WHERE d.category IN :categories " +
           "AND (:categoryId IS NULL OR d.category.id = :categoryId) " +
           "AND (:date IS NULL OR d.digestDate = :date) " +
           "AND (:keyword IS NULL OR LOWER(d.shortSummary) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(d.longSummary) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY d.digestDate DESC")
    List<CategoryDigest> findForUserCategories(
            @Param("categories") List<Category> categories,
            @Param("categoryId") UUID categoryId,
            @Param("date") LocalDate date,
            @Param("keyword") String keyword
    );
}
