package com.devteria.identityservice.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import com.devteria.identityservice.utils.XmlUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
@Slf4j
public class SitemapHelper {

    private final HtmlFetcherHelper htmlFetcherHelper;

    // Constants
    private static final int MAX_CONCURRENT_REQUESTS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 60;
    private static final int MAX_URLS_PER_REQUEST = 5000;

    // Thread pool for concurrent processing
    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS);

    /**
     * Crawl sitemaps song song
     */
    public List<String> crawlSitemapsConcurrently(List<String> sitemapUrls,
            AtomicInteger successCount,
            AtomicInteger errorCount,
            List<String> errors) {
        List<String> allUrls = new ArrayList<>();
        List<CompletableFuture<List<String>>> futures = new ArrayList<>();

        log.info("🔄 Bắt đầu crawl song song {} sitemaps", sitemapUrls.size());

        // Tạo các task crawl song song
        for (int i = 0; i < sitemapUrls.size(); i++) {
            String sitemapUrl = sitemapUrls.get(i);
            final int index = i + 1;

            CompletableFuture<List<String>> future = CompletableFuture
                    .supplyAsync(() -> {
                        log.info("🔄 [{}/{}] Đang crawl: {}", index, sitemapUrls.size(), sitemapUrl);
                        return crawlSingleSitemap(sitemapUrl, successCount, errorCount);
                    }, executorService)
                    .exceptionally(throwable -> {
                        String errorMsg = String.format("Lỗi khi crawl sitemap [%d/%d]: %s", index, sitemapUrls.size(),
                                sitemapUrl);
                        log.error("❌ {}", errorMsg, throwable);
                        errors.add(errorMsg + " - " + throwable.getMessage());
                        errorCount.incrementAndGet();
                        return new ArrayList<>();
                    });

            futures.add(future);
        }

        // Chờ tất cả task hoàn thành
        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            // Timeout cho toàn bộ quá trình
            allFutures.get(REQUEST_TIMEOUT_SECONDS * 2, TimeUnit.SECONDS);

            // Thu thập kết quả
            for (int i = 0; i < futures.size(); i++) {
                try {
                    List<String> urls = futures.get(i).get();
                    allUrls.addAll(urls);
                    log.info("✅ [{}/{}] Hoàn thành: {} URLs", i + 1, sitemapUrls.size(), urls.size());
                } catch (Exception e) {
                    log.error("❌ Lỗi khi lấy kết quả từ future {}", i + 1, e);
                }
            }

        } catch (TimeoutException e) {
            String errorMsg = "Timeout khi crawl sitemaps";
            log.error("❌ {}", errorMsg, e);
            errors.add(errorMsg);
        } catch (Exception e) {
            String errorMsg = "Lỗi khi xử lý concurrent crawling";
            log.error("❌ {}", errorMsg, e);
            errors.add(errorMsg + " - " + e.getMessage());
        }

        log.info("✅ Hoàn thành crawl song song: {} URLs tổng cộng", allUrls.size());
        return allUrls;
    }

    /**
     * Crawl sitemaps theo batch để xử lý số lượng lớn
     */
    public List<String> crawlSitemapsInBatches(List<String> sitemapUrls,
            AtomicInteger successCount,
            AtomicInteger errorCount,
            List<String> errors) {
        List<String> allUrls = new ArrayList<>();

        log.info("🔄 Bắt đầu crawl theo batch {} sitemaps (Large scale mode)", sitemapUrls.size());

        // Chia sitemap URLs thành các batch nhỏ
        int batchSize = Math.max(1, sitemapUrls.size() / 4); // Chia thành 4 batch
        List<List<String>> batches = new ArrayList<>();

        for (int i = 0; i < sitemapUrls.size(); i += batchSize) {
            int end = Math.min(i + batchSize, sitemapUrls.size());
            batches.add(sitemapUrls.subList(i, end));
        }

        log.info("📦 Chia thành {} batches, mỗi batch ~{} sitemaps", batches.size(), batchSize);

        // Xử lý từng batch
        for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
            List<String> batch = batches.get(batchIndex);
            log.info("🔄 Đang xử lý batch {}/{} ({} sitemaps)", batchIndex + 1, batches.size(), batch.size());

            try {
                // Crawl batch hiện tại
                List<String> batchUrls = crawlSitemapsConcurrently(batch, successCount, errorCount, errors);
                allUrls.addAll(batchUrls);

                log.info("✅ Batch {}/{} hoàn thành: {} URLs (Tổng: {})",
                        batchIndex + 1, batches.size(), batchUrls.size(), allUrls.size());

                // Kiểm tra giới hạn URLs
                if (allUrls.size() >= MAX_URLS_PER_REQUEST) {
                    log.warn("⚠️ Đã đạt giới hạn {} URLs, dừng crawl", MAX_URLS_PER_REQUEST);
                    break;
                }

                // Delay giữa các batch để tránh overload
                if (batchIndex < batches.size() - 1) {
                    log.info("⏳ Chờ 5 giây trước batch tiếp theo...");
                    Thread.sleep(5000);
                }

            } catch (Exception e) {
                log.error("❌ Lỗi khi xử lý batch {}/{}", batchIndex + 1, batches.size(), e);
                errors.add("Lỗi batch " + (batchIndex + 1) + ": " + e.getMessage());
            }
        }

        log.info("✅ Hoàn thành crawl theo batch: {} URLs tổng cộng", allUrls.size());
        return allUrls;
    }

    /**
     * Crawl một sitemap đơn lẻ
     */
    public List<String> crawlSingleSitemap(String sitemapUrl, AtomicInteger successCount, AtomicInteger errorCount) {
        try {
            String sitemapContent = htmlFetcherHelper.fetchWithRetry(sitemapUrl, "sitemap: " + sitemapUrl);

            if (sitemapContent != null) {
                List<String> urls = XmlUtils.extractUrlsFromSitemap(sitemapContent);
                successCount.incrementAndGet();
                log.debug("✅ Crawled {} URLs from {}", urls.size(), sitemapUrl);
                return urls;
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi crawl sitemap: {}", sitemapUrl, e);
            errorCount.incrementAndGet();
        }

        return new ArrayList<>();
    }

    /**
     * Cleanup method for graceful shutdown
     */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
