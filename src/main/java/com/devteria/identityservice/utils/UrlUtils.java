package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class UrlUtils {

    /**
     * Tự động thêm domain vào URL nếu thiếu
     */
    public static String ensureFullUrl(String url, String baseUrl) {
        if (url == null || url.isBlank()) {
            return url;
        }

        // Nếu URL đã có protocol (http/https), trả về nguyên bản
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        try {
            // Lấy domain từ baseUrl
            URI baseUri = new URI(baseUrl);
            String domain = baseUri.getScheme() + "://" + baseUri.getHost();

            // Nếu URL bắt đầu bằng /, thêm domain vào trước
            if (url.startsWith("/")) {
                return domain + url;
            }

            // Nếu URL không bắt đầu bằng /, thêm domain và / vào trước
            return domain + "/" + url;
        } catch (URISyntaxException e) {
            log.warn("Không thể parse baseUrl: {}", baseUrl);
            return url;
        }
    }

    /**
     * Kiểm tra xem attribute có phải là URL attribute không
     */
    public static boolean isUrlAttribute(String attribute) {
        if (attribute == null)
            return false;

        String lowerAttr = attribute.toLowerCase();
        return lowerAttr.equals("href") ||
                lowerAttr.equals("src") ||
                lowerAttr.equals("data-src") ||
                lowerAttr.equals("data-original") ||
                lowerAttr.equals("data-lazy-src") ||
                lowerAttr.equals("data-url") ||
                lowerAttr.equals("url") ||
                lowerAttr.equals("link") ||
                lowerAttr.startsWith("data-") && lowerAttr.contains("url");
    }

    /**
     * Tạo code từ URL
     */
    public static String generateCodeFromUrl(String url) {
        try {
            // Lấy phần cuối của URL (sau dấu / cuối cùng)
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];

            // Loại bỏ extension nếu có
            if (lastPart.contains(".")) {
                lastPart = lastPart.substring(0, lastPart.lastIndexOf("."));
            }

            // Tạo code từ lastPart
            String code = lastPart.toLowerCase()
                    .replaceAll("[^a-z0-9]", "-")
                    .replaceAll("-+", "-")
                    .replaceAll("^-|-$", "");

            // Thêm timestamp để đảm bảo unique
            return code + "-" + System.currentTimeMillis();

        } catch (Exception e) {
            log.warn("⚠️ Không thể tạo code từ URL: {}, sử dụng fallback", url);
            return "url-" + System.currentTimeMillis();
        }
    }

    /**
     * Trích xuất title từ URL
     */
    public static String extractTitleFromUrl(String url) {
        try {
            // Lấy phần cuối của URL
            String[] parts = url.split("/");
            String lastPart = parts[parts.length - 1];

            // Loại bỏ extension và query parameters
            if (lastPart.contains(".")) {
                lastPart = lastPart.substring(0, lastPart.lastIndexOf("."));
            }
            if (lastPart.contains("?")) {
                lastPart = lastPart.substring(0, lastPart.indexOf("?"));
            }

            // Chuyển đổi thành title
            String title = lastPart.replaceAll("-", " ")
                    .replaceAll("_", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            // Capitalize first letter of each word
            String[] words = title.split(" ");
            StringBuilder result = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    result.append(word.substring(0, 1).toUpperCase())
                            .append(word.substring(1).toLowerCase())
                            .append(" ");
                }
            }

            return result.toString().trim();

        } catch (Exception e) {
            log.warn("⚠️ Không thể trích xuất title từ URL: {}, sử dụng fallback", url);
            return "Auto-generated Title";
        }
    }
}
