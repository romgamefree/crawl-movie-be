package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StringUtils {

    /**
     * Kiểm tra string có null hoặc blank không
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Kiểm tra string có null hoặc empty không
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Tạo metadata string từ title và description
     */
    public static String buildMetadataString(String title, String description) {
        StringBuilder sb = new StringBuilder();
        if (!isNullOrBlank(title)) {
            sb.append("Title: ").append(title.trim());
        }
        if (!isNullOrBlank(description)) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("Description: ").append(description.trim());
        }
        return sb.toString();
    }

    /**
     * Truncate string với độ dài tối đa
     */
    public static String truncate(String str, int maxLength) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }

    /**
     * Clean và normalize string
     */
    public static String cleanString(String str) {
        if (isNullOrEmpty(str)) {
            return str;
        }
        return str.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[\\r\\n\\t]", " ");
    }

    /**
     * Generate slug
     */
    public static String generateSlug(String title) {
        if (title == null || title.trim().isEmpty()) {
            return "unknown-" + System.currentTimeMillis();
        }

        // Xử lý tiếng Thái và các ký tự Unicode
        String slug = title.toLowerCase()
                // Giữ lại chữ cái tiếng Thái, chữ cái Latin, số, khoảng trắng và dấu gạch ngang
                .replaceAll("[^\\u0E00-\\u0E7F\\u0E80-\\u0EFFa-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        // Kiểm tra slug có hợp lệ không
        if (slug == null || slug.isEmpty() || slug.isBlank()) {
            return "unknown-" + System.currentTimeMillis();
        }

        // Nếu slug chỉ có dấu gạch ngang, trả về unknown với timestamp
        if (slug.replaceAll("-", "").isEmpty()) {
            return "unknown-" + System.currentTimeMillis();
        }

        return slug;
    }
}
