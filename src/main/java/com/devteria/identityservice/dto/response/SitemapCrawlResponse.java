package com.devteria.identityservice.dto.response;

import lombok.*;

import java.time.Duration;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SitemapCrawlResponse {
    private int totalUrls;
    private List<String> urls;
    private int savedUrls;
    private int skippedUrls;
    private int successCount;
    private int errorCount;
    private Duration executionTime;
    private String sitemapUrl;
    private String selectorId;
    private String status;
    private String message;
    private List<String> errors;
}
