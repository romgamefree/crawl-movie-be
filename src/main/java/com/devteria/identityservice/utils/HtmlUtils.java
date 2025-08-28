package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HtmlUtils {

    /**
     * Parse HTML string thành Document
     */
    public static Document parseHtml(String html, String baseUrl) {
        return Jsoup.parse(html, baseUrl);
    }

    /**
     * Trích xuất title từ Document
     */
    public static String extractTitle(Document doc) {
        return doc.title();
    }

    /**
     * Trích xuất description từ meta tag
     */
    public static String extractDescription(Document doc) {
        Element meta = doc.selectFirst("meta[name=description]");
        if (meta != null) {
            String content = meta.attr("content");
            if (content != null && !content.isBlank()) {
                return content;
            }
        }
        return null;
    }

    /**
     * Lấy text content của element
     */
    public static String getElementText(Element element) {
        return element != null ? element.text() : null;
    }

    /**
     * Lấy attribute value của element
     */
    public static String getElementAttribute(Element element, String attribute) {
        return element != null ? element.attr(attribute) : null;
    }

    /**
     * Kiểm tra element có text content hợp lệ không
     */
    public static boolean hasValidText(Element element) {
        String text = getElementText(element);
        return text != null && !text.isBlank();
    }

    /**
     * Kiểm tra element có attribute hợp lệ không
     */
    public static boolean hasValidAttribute(Element element, String attribute) {
        String value = getElementAttribute(element, attribute);
        return value != null && !value.isBlank();
    }

    /**
     * Lấy danh sách text từ Elements
     */
    public static List<String> getElementsText(Elements elements) {
        return elements.stream()
                .map(Element::text)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách attribute values từ Elements
     */
    public static List<String> getElementsAttribute(Elements elements, String attribute) {
        return elements.stream()
                .map(element -> element.attr(attribute))
                .filter(value -> value != null && !value.isBlank())
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra có phần tử nào có text hợp lệ không
     */
    public static boolean hasAnyValidText(Elements elements) {
        return elements.stream().anyMatch(HtmlUtils::hasValidText);
    }

    /**
     * Kiểm tra có phần tử nào có attribute hợp lệ không
     */
    public static boolean hasAnyValidAttribute(Elements elements, String attribute) {
        return elements.stream().anyMatch(element -> hasValidAttribute(element, attribute));
    }
}
