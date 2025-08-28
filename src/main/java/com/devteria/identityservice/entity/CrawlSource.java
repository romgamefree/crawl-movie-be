package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class CrawlSource extends BaseEntity{
    @Column(unique = true, nullable = false)
    String code; // ví dụ: ophim

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String baseUrl;

    @Builder.Default
    Boolean enabled = true;

    @Builder.Default
    Boolean inserted = false; // Đánh dấu đã insert hay chưa

    @Column(columnDefinition = "TEXT")
    String note;

    // Quan hệ Many-to-One với Selector (nhiều CrawlSource dùng chung 1 Selector)
    @ManyToOne
    @JoinColumn(name = "selector_id")
    Selector selector;
}


