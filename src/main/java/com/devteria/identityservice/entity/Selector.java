package com.devteria.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Selector extends BaseEntity{
    // Quan hệ One-to-Many với CrawlSource (nhiều CrawlSource dùng chung 1 Selector)
    @OneToMany(mappedBy = "selector", cascade = CascadeType.ALL)
    Set<CrawlSource> crawlSources;

    // Khóa chức năng selector, ví dụ: movie.title, movie.episodes, episode.links
    @Column(nullable = false)
    String name;

    String note;

    // Quan hệ One-to-Many với SelectorItem
    @OneToMany(mappedBy = "selector", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<SelectorItem> selectorItems;
}


