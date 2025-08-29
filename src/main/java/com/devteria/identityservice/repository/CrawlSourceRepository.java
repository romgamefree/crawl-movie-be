package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.CrawlSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CrawlSourceRepository extends JpaRepository<CrawlSource, String> {
    boolean existsByCode(String code);

    boolean existsByBaseUrl(String baseUrl);

    /**
     * Find a crawl source by its base URL
     */
    Optional<CrawlSource> findByBaseUrl(String baseUrl);

    /**
     * Tìm tất cả crawl sources được enabled
     */
    List<CrawlSource> findByEnabledTrue();

    /**
     * Tìm tất cả crawl sources chưa được insert
     */
    List<CrawlSource> findByInsertedFalse();

    /**
     * Tìm tất cả crawl sources đã được insert
     */
    List<CrawlSource> findByInsertedTrue();

    /**
     * Tìm tất cả crawl sources được enabled và chưa được insert
     */
    List<CrawlSource> findByEnabledTrueAndInsertedFalse();

    /**
     * Kiểm tra nhiều URLs cùng lúc để tối ưu performance
     */
    @Query("SELECT cs.baseUrl FROM CrawlSource cs WHERE cs.baseUrl IN :urls")
    Set<String> findExistingUrls(@Param("urls") List<String> urls);

    /**
     * Tìm trực tiếp Set<String> IDs của crawl sources có enabled = true và inserted
     * = false
     * Tối ưu hơn so với findByEnabledTrueAndInsertedFalse() vì chỉ lấy IDs
     */
    @Query("SELECT cs.id FROM CrawlSource cs WHERE cs.enabled = true AND cs.inserted = false")
    Set<String> findEnabledAndNotInsertedIds();

    Optional<CrawlSource> findFirstBySelectorId(String selectorId);

    /**
     * Lấy IDs theo checkpoint (lastId) và giới hạn bởi pageable (size)
     */
    @Query("SELECT cs.id FROM CrawlSource cs WHERE cs.enabled = true AND cs.inserted = false AND (:lastId IS NULL OR cs.id > :lastId) ORDER BY cs.id ASC")
    List<String> findEnabledNotInsertedIdsAfter(@Param("lastId") String lastId, Pageable pageable);
}
