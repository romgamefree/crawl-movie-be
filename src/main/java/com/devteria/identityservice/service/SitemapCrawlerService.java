package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.SitemapCrawlRequest;
import com.devteria.identityservice.dto.response.SitemapCrawlResponse;
import com.devteria.identityservice.entity.CrawlSource;
import com.devteria.identityservice.entity.Selector;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.repository.CrawlSourceRepository;
import com.devteria.identityservice.repository.SelectorRepository;
import com.devteria.identityservice.helpers.HtmlFetcherHelper;
import com.devteria.identityservice.helpers.SitemapHelper;
import com.devteria.identityservice.utils.XmlUtils;
import com.devteria.identityservice.utils.UrlUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class SitemapCrawlerService {

    final SelectorRepository selectorRepository;
    final CrawlSourceRepository crawlSourceRepository;
    final HtmlFetcherHelper htmlFetcherHelper;
    final SitemapHelper sitemapHelper;

    // Constants for configuration
    private static final int BATCH_SIZE = 1000; // Xử lý theo batch 1000 URLs

    /**
     * Lưu danh sách URLs vào CrawlSource với selector (Batch Processing)
     */
    private List<String> saveUrlsToCrawlSource(List<String> urls, Selector selector) {
        List<String> savedUrls = new ArrayList<>();

        log.info("🔄 Bắt đầu lưu {} URLs vào CrawlSource (Batch size: {})", urls.size(), BATCH_SIZE);

        // Xử lý theo batch để tránh memory issues
        for (int batchStart = 0; batchStart < urls.size(); batchStart += BATCH_SIZE) {
            int batchEnd = Math.min(batchStart + BATCH_SIZE, urls.size());
            List<String> batchUrls = urls.subList(batchStart, batchEnd);

            log.info("📦 Xử lý batch {}/{} (URLs {}-{})",
                    (batchStart / BATCH_SIZE) + 1,
                    (urls.size() + BATCH_SIZE - 1) / BATCH_SIZE,
                    batchStart + 1, batchEnd);

            List<String> batchSavedUrls = processBatch(batchUrls, selector);
            savedUrls.addAll(batchSavedUrls);

            // Log progress sau mỗi batch
            log.info("✅ Batch hoàn thành: {} URLs lưu thành công (Tổng: {}/{})",
                    batchSavedUrls.size(), savedUrls.size(), urls.size());

            // Thêm delay nhỏ giữa các batch để tránh overload database
            if (batchEnd < urls.size()) {
                try {
                    Thread.sleep(1000); // 1 giây delay giữa các batch
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        log.info("✅ Hoàn thành lưu URLs: {} lưu thành công, {} bỏ qua",
                savedUrls.size(), urls.size() - savedUrls.size());

        return savedUrls;
    }

    /**
     * Xử lý một batch URLs
     */
    private List<String> processBatch(List<String> batchUrls, Selector selector) {
        List<String> batchSavedUrls = new ArrayList<>();
        List<CrawlSource> entitiesToSave = new ArrayList<>();

        // Phase 1: Bulk check existing URLs
        Set<String> existingUrls = crawlSourceRepository.findExistingUrls(batchUrls);
        log.debug("🔍 Bulk check: {} URLs đã tồn tại trong {} URLs", existingUrls.size(), batchUrls.size());

        // Phase 2: Tạo entities cho URLs mới
        for (String url : batchUrls) {
            try {
                // Kiểm tra xem URL đã tồn tại chưa
                if (existingUrls.contains(url)) {
                    log.debug("⏭️ URL đã tồn tại, bỏ qua: {}", url);
                    continue;
                }

                // Tạo CrawlSource entity
                CrawlSource crawlSource = CrawlSource.builder()
                        .code(UrlUtils.generateCodeFromUrl(url))
                        .name(UrlUtils.extractTitleFromUrl(url))
                        .baseUrl(url)
                        .enabled(true)
                        .note("Auto-generated from sitemap crawl with selector: " + selector.getName())
                        .selector(selector)
                        .build();

                entitiesToSave.add(crawlSource);

            } catch (Exception e) {
                log.error("❌ Lỗi khi tạo entity cho URL: {}", url, e);
            }
        }

        // Phase 2: Bulk save entities
        if (!entitiesToSave.isEmpty()) {
            try {
                List<CrawlSource> savedEntities = crawlSourceRepository.saveAll(entitiesToSave);
                batchSavedUrls = savedEntities.stream()
                        .map(CrawlSource::getBaseUrl)
                        .toList();

                log.debug("✅ Bulk save thành công: {} entities", savedEntities.size());

            } catch (Exception e) {
                log.error("❌ Lỗi khi bulk save batch", e);

                // Fallback: save từng entity một
                for (CrawlSource entity : entitiesToSave) {
                    try {
                        CrawlSource savedEntity = crawlSourceRepository.save(entity);
                        batchSavedUrls.add(savedEntity.getBaseUrl());
                    } catch (Exception ex) {
                        log.error("❌ Lỗi khi save entity: {}", entity.getBaseUrl(), ex);
                    }
                }
            }
        }

        return batchSavedUrls;
    }

    public SitemapCrawlResponse crawlNung2HddSitemapWithSelector(SitemapCrawlRequest request) {
        Instant startTime = Instant.now();
        List<String> allUrls = new ArrayList<>();
        List<String> savedUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Selector selector = selectorRepository.findById(request.getSelectorId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        try {
            // 1. Lấy nội dung sitemap gốc
            String sitemapContent = htmlFetcherHelper.fetchWithRetry(request.getSitemapUrl(), "sitemap index");

            if (sitemapContent != null) {
                // 2. Extract tất cả sitemap con
                List<String> sitemapUrls = XmlUtils.extractUrlsFromSitemap(sitemapContent);

                // 3. Filter chỉ giữ lại post-sitemap*.xml
                List<String> postSitemaps = sitemapUrls.stream()
                        .filter(url -> url.matches("https://www\\.nung2-hdd\\.com/post-sitemap\\d*\\.xml"))
                        .toList();

                log.info("📑 Tìm thấy {} post-sitemaps", postSitemaps.size());

                // 4. Crawl song song các sitemap này
                allUrls = sitemapHelper.crawlSitemapsConcurrently(postSitemaps, successCount, errorCount, errors);
            }

            savedUrls = saveUrlsToCrawlSource(allUrls, selector);
        } catch (Exception e) {
            log.error("❌ Lỗi khi crawl nung2-hdd movie URLs", e);
        }

        log.info("🎬 Tổng số movie URLs cào được: {}", allUrls.size());

        Duration executionTime = Duration.between(startTime, Instant.now());

        return SitemapCrawlResponse.builder()
                .totalUrls(allUrls.size())
                .urls(savedUrls)
                .savedUrls(savedUrls.size())
                .skippedUrls(allUrls.size() - savedUrls.size())
                .successCount(successCount.get())
                .errorCount(errorCount.get())
                .executionTime(executionTime)
                .sitemapUrl(request.getSitemapUrl())
                .selectorId(request.getSelectorId())
                .status(errorCount.get() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS")
                .message(String.format("Crawl hoàn thành: %d URLs, %d thành công, %d lỗi",
                        allUrls.size(), successCount.get(), errorCount.get()))
                .errors(errors)
                .build();
    }


    public SitemapCrawlResponse crawl123HdSitemapWithSelector(SitemapCrawlRequest request) {
        Instant startTime = Instant.now();
        List<String> allUrls = new ArrayList<>();
        List<String> savedUrls = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        Selector selector = selectorRepository.findById(request.getSelectorId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        try {
            // 1. Lấy nội dung sitemap gốc
            String sitemapContent = htmlFetcherHelper.fetchWithRetry(request.getSitemapUrl(), "sitemap index");

            if (sitemapContent != null) {
                // 2. Extract tất cả sitemap con
                List<String> sitemapUrls = XmlUtils.extractUrlsFromSitemap(sitemapContent);

                // 3. Filter chỉ giữ lại post-sitemap*.xml
                List<String> postSitemaps = sitemapUrls.stream()
                        .filter(url -> url.matches("https://www\\.123hdtv\\.com/wp-sitemap-posts-post-\\d+\\.xml"))
                        .toList();


                log.info("📑 Tìm thấy {} post-sitemaps", postSitemaps.size());

                // 4. Crawl song song các sitemap này
                allUrls = sitemapHelper.crawlSitemapsConcurrently(postSitemaps, successCount, errorCount, errors);
            }

            savedUrls = saveUrlsToCrawlSource(allUrls, selector);
        } catch (Exception e) {
            log.error("❌ Lỗi khi crawl nung2-hdd movie URLs", e);
        }

        log.info("🎬 Tổng số movie URLs cào được: {}", allUrls.size());

        Duration executionTime = Duration.between(startTime, Instant.now());

        return SitemapCrawlResponse.builder()
                .totalUrls(allUrls.size())
                .urls(savedUrls)
                .savedUrls(savedUrls.size())
                .skippedUrls(allUrls.size() - savedUrls.size())
                .successCount(successCount.get())
                .errorCount(errorCount.get())
                .executionTime(executionTime)
                .sitemapUrl(request.getSitemapUrl())
                .selectorId(request.getSelectorId())
                .status(errorCount.get() == 0 ? "SUCCESS" : "PARTIAL_SUCCESS")
                .message(String.format("Crawl hoàn thành: %d URLs, %d thành công, %d lỗi",
                        allUrls.size(), successCount.get(), errorCount.get()))
                .errors(errors)
                .build();
    }
}
