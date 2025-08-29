package com.devteria.identityservice.configuration;

import com.devteria.identityservice.dto.request.SitemapCrawlRequest;
import com.devteria.identityservice.entity.Config;
import com.devteria.identityservice.repository.ConfigRepository;
import com.devteria.identityservice.service.CrawlSourceService;
import com.devteria.identityservice.service.SitemapCrawlerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static com.devteria.identityservice.constant.AppConfigKeys.*;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DynamicSchedulerConfig implements SchedulingConfigurer {

    private final ConfigRepository configRepository;
    private final CrawlSourceService crawlSourceService;
    private final SitemapCrawlerService sitemapCrawlerService;


    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // Crawl Sitemap (placeholder – sẽ nối vào SitemapCrawlerService khi có request
        // chuẩn)
        taskRegistrar.addTriggerTask(
                this::crawlSitemapJob,
                triggerContext -> {
                    Long minutes = getScheduleValue(SCHEDULE_CRAWL_SITEMAP);
                    if (minutes == null)
                        minutes = 30L; // default 30 phút

                    Instant lastCompletion = triggerContext.lastCompletion();
                    Instant base = lastCompletion != null ? lastCompletion : Instant.now();
                    return base.plus(Duration.ofMinutes(minutes));
                });

        // Crawl Movie (insert tất cả movies từ crawl sources enabled & chưa insert)
        taskRegistrar.addTriggerTask(
                this::crawlMovieJob,
                triggerContext -> {
                    Long minutes = getScheduleValue(SCHEDULE_CRAWL_MOVIE);
                    if (minutes == null)
                        minutes = 60L; // default 60 phút

                    Instant lastCompletion = triggerContext.lastCompletion();
                    Instant base = lastCompletion != null ? lastCompletion : Instant.now();
                    return base.plus(Duration.ofMinutes(minutes));
                });
    }

    private Long getScheduleValue(String key) {
        return configRepository.findByKey(key)
                .map(c -> {
                    try {
                        return Long.valueOf(c.getValue());
                    } catch (NumberFormatException ex) {
                        log.warn("Giá trị config '{}' không hợp lệ: {}", key, c.getValue());
                        return null;
                    }
                })
                .orElse(null);
    }

    private void crawlSitemapJob() {
        log.info("🚀 Run crawl sitemap at {}", new Date());
        Config configSitemap = configRepository.findByKey(SITEMAP_URL_123HD).orElse(null);
        Config configSelector = configRepository.findByKey(SELECTOR_123HD).orElse(null);

        if(configSitemap == null || configSelector == null) {
            log.warn("Chưa tìm thấy config 123hdtv.sitemap.url hoá 123hdtv.selector");
            return;
        }
        SitemapCrawlRequest request = SitemapCrawlRequest.builder()
                .sitemapUrl(configSitemap.getValue())
                .selectorId(configSelector.getValue())
                .build();

        sitemapCrawlerService.crawl123HdSitemapWithSelector(request);
        log.info("✅ Completed crawl sitemap job at {}", new Date());
    }

    private void crawlMovieJob() {
        log.info("🎬 Run crawl movie at {}", new Date());
        try {
            crawlSourceService.insertAllMovies();
            log.info("✅ Completed crawl movie job at {}", new Date());
        } catch (Exception ex) {
            log.error("Lỗi khi chạy crawl movie job", ex);
        }
    }
}
