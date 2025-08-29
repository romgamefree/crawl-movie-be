package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.CrawlSourceRequest;
import com.devteria.identityservice.dto.request.SitemapCrawlRequest;
import com.devteria.identityservice.dto.response.BulkInsertResponse;
import com.devteria.identityservice.dto.response.CrawlSourceResponse;
import com.devteria.identityservice.dto.response.SitemapCrawlResponse;
import com.devteria.identityservice.service.CrawlSourceService;
import com.devteria.identityservice.service.SitemapCrawlerService;
import com.devteria.identityservice.service.VideoLinkExtractor;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/crawl-sources")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CrawlSourceController {
    CrawlSourceService service;
    SitemapCrawlerService sitemapCrawlerService;
    VideoLinkExtractor videoAdSkipper;

    @PostMapping
    ApiResponse<CrawlSourceResponse> create(@RequestBody @Valid CrawlSourceRequest request) {
        return ApiResponse.<CrawlSourceResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<CrawlSourceResponse> update(@PathVariable String id, @RequestBody @Valid CrawlSourceRequest request) {
        return ApiResponse.<CrawlSourceResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping("/{id}")
    ApiResponse<CrawlSourceResponse> get(@PathVariable String id) {
        return ApiResponse.<CrawlSourceResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }

    @PostMapping("/123hd/crawl-sitemap")
    ApiResponse<SitemapCrawlResponse> crawl123HdSitemapLargeScale(@RequestBody @Valid SitemapCrawlRequest request) {
        return ApiResponse.<SitemapCrawlResponse>builder()
                .result(sitemapCrawlerService.crawl123HdSitemapWithSelector(request)).build();
    }

    @PostMapping("/insert-movies-by-urls")
    ApiResponse<BulkInsertResponse> insertMoviesFromCrawlSourceUrls(@RequestBody List<String> crawlSourceUrls) {
        // Yêu cầu: API này phải chạy lại ngay cả khi đã inserted (force = true)
        BulkInsertResponse response = service.insertFromCrawlSourceUrls(crawlSourceUrls, true);
        return ApiResponse.<BulkInsertResponse>builder().result(response).build();
    }

    @PostMapping("/insert-all-movies")
    ApiResponse<BulkInsertResponse> insertAllMovies() {
        BulkInsertResponse response = service.insertAllMovies();
        return ApiResponse.<BulkInsertResponse>builder().result(response).build();
    }

    @PostMapping("/nung2-hdd/crawl-sitemap")
    ApiResponse<SitemapCrawlResponse> crawlNung2HddSitemapLargeScale(@RequestBody @Valid SitemapCrawlRequest request) {
        SitemapCrawlResponse result = sitemapCrawlerService.crawlNung2HddSitemapWithSelector(request);
        return ApiResponse.<SitemapCrawlResponse>builder().result(result).build();
    }

    @PostMapping("/nung2-hdd/get-movie-url")
    ApiResponse<Void> getMovieUrl() {
        videoAdSkipper.extractVideoLink(
                "https://www.stream1689.com/nung2hdd.php?v=gn0M3fLHTt&lang=%E0%B8%9E%E0%B8%B2%E0%B8%81%E0%B8%A2%E0%B9%8C%E0%B9%84%E0%B8%97%E0%B8%A2");
        return ApiResponse.<Void>builder().build();
    }
}
