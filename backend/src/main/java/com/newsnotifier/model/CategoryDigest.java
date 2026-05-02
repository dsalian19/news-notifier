package com.newsnotifier.model;

import com.newsnotifier.config.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "category_digests",
    uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "digest_date"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDigest {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "digest_date", nullable = false)
    private LocalDate digestDate;

    @Column(name = "short_summary", columnDefinition = "TEXT")
    private String shortSummary;

    @Column(name = "long_summary", columnDefinition = "TEXT")
    private String longSummary;

    @Convert(converter = StringListConverter.class)
    @Column(name = "article_urls", columnDefinition = "jsonb")
    private List<String> articleUrls;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
