package com.devteria.identityservice.utils;

import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class RegexUtils {

    /**
     * Trích xuất ID từ link embed 24playerhd
     * Ví dụ:
     * https://hot.24playerhd.com/public/dist/index.php?id=6718a8fbd0fb4944a72870bc3ba672bc&seek=0&seekmin=0
     * Trả về: 6718a8fbd0fb4944a72870bc3ba672bc
     */
    public static String extractIdFromEmbedLinkFromHot24PlayerHd(String embedLink) {
        if (StringUtils.isNullOrBlank(embedLink)) {
            throw new IllegalArgumentException("Link embed không được để trống");
        }

        // Regex bắt mọi giá trị của id cho đến dấu &
        Pattern pattern = Pattern.compile("id=([^&]+)");
        Matcher matcher = pattern.matcher(embedLink);

        if (matcher.find()) {
            String id = matcher.group(1);
            log.info("✅ Đã trích xuất ID: {} từ link: {}", id, embedLink);
            return id;
        } else {
            log.error("Không thể trích xuất ID từ link embed: " + embedLink);
            throw new AppException(ErrorCode.CAN_NOT_EXTRACT_ID_FROM_URL);
        }
    }

    /**
     * Trích xuất ID từ link embed player.stream1689
     * Ví dụ:
     * https://player.stream1689.com/p2p/ca602ac9d3be2630d2935b6fddff33ce
     * Trả về: ca602ac9d3be2630d2935b6fddff33ce
     */
    public static String extractIdFromEmbedLinkFromStream1689(String embedLink) {
        if (StringUtils.isNullOrBlank(embedLink)) {
            throw new IllegalArgumentException("Link embed không được để trống");
        }

        // Regex: lấy phần sau /p2p/
        Pattern pattern = Pattern.compile("/p2p/([a-zA-Z0-9]+)");
        Matcher matcher = pattern.matcher(embedLink);

        if (matcher.find()) {
            String id = matcher.group(1);
            log.info("✅ Đã trích xuất ID: {} từ link: {}", id, embedLink);
            return id;
        } else {
            log.error("Không thể trích xuất ID từ link embed: " + embedLink);
            throw new AppException(ErrorCode.CAN_NOT_EXTRACT_ID_FROM_URL);
        }
    }



    /**
     * Trích xuất href từ các thẻ <a> trong HTML
     */
    public static String extractHrefFromLinks(String htmlContent) {
        if (StringUtils.isNullOrBlank(htmlContent)) {
            return htmlContent;
        }

        // Tìm href attribute trong thẻ <a>
        Pattern pattern = Pattern.compile("href=[\"']([^\"']+)[\"']");
        Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Trích xuất text từ các thẻ <a> trong HTML
     */
    public static String extractTextFromLinks(String htmlContent) {
        if (StringUtils.isNullOrBlank(htmlContent)) {
            return htmlContent;
        }

        // Tìm tất cả text content trong thẻ <a>
        Pattern pattern = Pattern.compile("<a[^>]*>([^<]+)</a>");
        Matcher matcher = pattern.matcher(htmlContent);

        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append(", ");
            }
            result.append(matcher.group(1).trim());
        }

        // Nếu không tìm thấy thẻ <a>, trả về text gốc
        return result.length() > 0 ? result.toString() : htmlContent;
    }

    /**
     * Trích xuất năm từ text
     */
    public static String extractYearFromText(String text) {
        if (StringUtils.isNullOrBlank(text)) {
            return null;
        }

        // Tìm năm trong text (4 chữ số liên tiếp)
        Pattern pattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }
}
