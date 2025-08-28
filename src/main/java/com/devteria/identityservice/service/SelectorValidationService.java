package com.devteria.identityservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import com.devteria.identityservice.helpers.HtmlFetcherHelper;
import com.devteria.identityservice.utils.HtmlUtils;
import com.devteria.identityservice.utils.StringUtils;
import com.devteria.identityservice.utils.UrlUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelectorValidationService {
    private final HtmlFetcherHelper htmlFetcherHelper;

    public boolean testSelector(String baseUrl, String query, String attribute, boolean isList) {
        try {
            String html = htmlFetcherHelper.fetchUrl(baseUrl);
            Document doc = HtmlUtils.parseHtml(html, baseUrl);

            if (isList) {
                // Lấy tất cả phần tử
                Elements elements = doc.select(query);
                if (elements.isEmpty())
                    return false;

                if (StringUtils.isNullOrBlank(attribute)) {
                    // Kiểm tra text content
                    boolean hasValidText = HtmlUtils.hasAnyValidText(elements);
                    if (hasValidText) {
                        List<String> texts = HtmlUtils.getElementsText(elements);
                        log.info("Tìm thấy {} phần tử với text: {}", texts.size(), texts);
                    }
                    return hasValidText;
                } else {
                    // Kiểm tra attribute
                    boolean hasValidAttr = HtmlUtils.hasAnyValidAttribute(elements, attribute);
                    if (hasValidAttr) {
                        List<String> attrs = HtmlUtils.getElementsAttribute(elements, attribute);
                        // Nếu attribute là URL, thêm domain nếu thiếu
                        if (UrlUtils.isUrlAttribute(attribute)) {
                            attrs = attrs.stream()
                                    .map(val -> UrlUtils.ensureFullUrl(val, baseUrl))
                                    .toList();
                        }
                        log.info("Tìm thấy {} phần tử với {}: {}", attrs.size(), attribute, attrs);
                    }
                    return hasValidAttr;
                }
            } else {
                // Chỉ lấy phần tử đầu tiên
                Element element = doc.selectFirst(query);
                if (element == null)
                    return false;

                if (StringUtils.isNullOrBlank(attribute)) {
                    // Kiểm tra text content
                    String text = HtmlUtils.getElementText(element);
                    log.info("Text: {}", text);
                    return HtmlUtils.hasValidText(element);
                } else {
                    // Kiểm tra attribute
                    String val = HtmlUtils.getElementAttribute(element, attribute);
                    // Nếu attribute là URL, thêm domain nếu thiếu
                    if (UrlUtils.isUrlAttribute(attribute)) {
                        val = UrlUtils.ensureFullUrl(val, baseUrl);
                    }
                    log.info("Attribute {}: {}", attribute, val);
                    return HtmlUtils.hasValidAttribute(element, attribute);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to test selector {} on {}: {}", query, baseUrl, e.getMessage());
            return false;
        }
    }
}
