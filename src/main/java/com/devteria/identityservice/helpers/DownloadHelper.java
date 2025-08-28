package com.devteria.identityservice.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.devteria.identityservice.utils.FileUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DownloadHelper {

    private final RestTemplate restTemplate;

    // Constants
    private static final String USER_AGENT = "Mozilla/5.0";

    /**
     * Tải nội dung từ URL
     */
    public String downloadContent(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);

            HttpEntity<byte[]> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new String(response.getBody(), "UTF-8");
            } else {
                throw new RuntimeException("Failed to fetch " + url + " - " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi tải nội dung từ {}: {}", url, e.getMessage());
            throw new RuntimeException("Failed to fetch " + url, e);
        }
    }

    /**
     * Tải và lưu file từ URL
     */
    public void downloadAndSaveFile(String fileUrl, String savePath) {
        try {
            log.info("📥 Đang tải: {}", fileUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", USER_AGENT);

            HttpEntity<byte[]> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(fileUrl, HttpMethod.GET, entity, byte[].class);

            if (response.getStatusCode().is2xxSuccessful()) {
                FileUtils.saveFileBytes(savePath, response.getBody());
            } else {
                throw new RuntimeException("Failed to fetch " + fileUrl + " - " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Lỗi khi tải file {}: {}", fileUrl, e.getMessage());
            throw new RuntimeException("Failed to save " + fileUrl, e);
        }
    }
}
