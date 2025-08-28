package com.devteria.identityservice.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class HtmlFetcherHelper {

    private final RestTemplate restTemplate;
    private final SeleniumHelper seleniumHelper;

    // Constants
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    private static final int REQUEST_TIMEOUT_SECONDS = 60;
    private static final int RETRY_ATTEMPTS = 3;
    private static final long RATE_LIMIT_DELAY_MS = 20;

    /**
     * Fetch HTML từ URL với retry mechanism
     */
    public String fetchWithRetry(String url, String description) {
        for (int attempt = 1; attempt <= RETRY_ATTEMPTS; attempt++) {
            try {
                String content = fetchUrl(url);
                if (content != null) {
                    return content;
                }
            } catch (Exception e) {
                log.warn("⚠️ Lần thử {} thất bại cho {}: {}", attempt, description, e.getMessage());

                if (attempt < RETRY_ATTEMPTS) {
                    try {
                        // Exponential backoff: 1s, 2s, 4s
                        long delay = (long) Math.pow(2, attempt - 1) * 1000;
                        log.debug("⏳ Chờ {}ms trước lần thử tiếp theo", delay);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("❌ Tất cả {} lần thử đều thất bại cho {}", RETRY_ATTEMPTS, description);
        return null;
    }

    /**
     * Fetch HTML từ URL (sử dụng RestTemplate)
     */
    public String fetchUrl(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("User-Agent", USER_AGENT);
        headers.set("Accept", "application/xml, text/xml, */*");
        headers.set("Accept-Language", "vi-VN,vi;q=0.9,en;q=0.8");
        headers.set("Cache-Control", "no-cache");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Rate limiting
                Thread.sleep(RATE_LIMIT_DELAY_MS);
                return response.getBody();
            }

        } catch (ResourceAccessException e) {
            log.warn("⚠️ Timeout hoặc lỗi kết nối khi fetch URL: {}", url);
            throw e;
        } catch (RestClientException e) {
            log.warn("⚠️ Lỗi HTTP khi fetch URL: {}", url);
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread bị interrupt", e);
        }

        return null;
    }

    /**
     * Fetch HTML với JavaScript rendering (sử dụng Selenium)
     */
    public String fetchUrlWithJs(String url) {
        log.info("🔄 Sử dụng Selenium để fetch HTML với JavaScript từ: {}", url);
        return seleniumHelper.fetchHtmlWithJs(url);
    }

    /**
     * Fetch HTML với JavaScript và chờ element cụ thể
     */
    public String fetchUrlWithJsAndWaitForElement(String url, String elementSelector) {
        log.info("🔄 Sử dụng Selenium để fetch HTML và chờ element: {} từ: {}", elementSelector, url);
        return seleniumHelper.fetchHtmlWithJsAndWaitForElement(url, elementSelector);
    }
}
