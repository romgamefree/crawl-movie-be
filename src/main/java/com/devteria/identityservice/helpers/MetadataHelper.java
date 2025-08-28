package com.devteria.identityservice.helpers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import com.devteria.identityservice.utils.HtmlUtils;
import com.devteria.identityservice.utils.MetadataUtils;
import com.devteria.identityservice.utils.MetadataType;
import com.devteria.identityservice.utils.MetadataResponse;

import java.util.List;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class MetadataHelper {

    private final HtmlFetcherHelper htmlFetcherHelper;

    /**
     * Fetch metadata theo danh sách types được chỉ định
     */
    public MetadataResponse fetchMetadata(String baseUrl, List<MetadataType> types) {
        try {
            String html = htmlFetcherHelper.fetchUrl(baseUrl);
            Document doc = HtmlUtils.parseHtml(html, baseUrl);
            return MetadataUtils.extractMetadata(doc, baseUrl, types);

        } catch (Exception e) {
            log.warn("Failed to fetch metadata from {}: {}", baseUrl, e.getMessage());
            return MetadataResponse.builder()
                    .url(baseUrl)
                    .requestedTypes(types)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * Fetch tất cả metadata
     */
    public MetadataResponse fetchAllMetadata(String baseUrl) {
        return fetchMetadata(baseUrl, Arrays.asList(MetadataType.values()));
    }

    /**
     * Fetch metadata cơ bản (title, description, h1, h2)
     */
    public MetadataResponse fetchBasicMetadata(String baseUrl) {
        return fetchMetadata(baseUrl, Arrays.asList(MetadataType.BASIC_METADATA));
    }

    /**
     * Fetch SEO metadata
     */
    public MetadataResponse fetchSeoMetadata(String baseUrl) {
        return fetchMetadata(baseUrl, Arrays.asList(MetadataType.SEO_METADATA));
    }

    /**
     * Fetch social media metadata
     */
    public MetadataResponse fetchSocialMetadata(String baseUrl) {
        return fetchMetadata(baseUrl, Arrays.asList(MetadataType.SOCIAL_METADATA));
    }

    /**
     * Fetch content metadata
     */
    public MetadataResponse fetchContentMetadata(String baseUrl) {
        return fetchMetadata(baseUrl, Arrays.asList(MetadataType.CONTENT_METADATA));
    }

    /**
     * Fetch title và description (backward compatibility)
     */
    public String fetchTitleAndDescription(String baseUrl) {
        MetadataResponse response = fetchBasicMetadata(baseUrl);
        if (!response.isSuccess()) {
            return null;
        }
        
        return response.getSummary();
    }

    /**
     * Fetch chỉ title
     */
    public String fetchTitle(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.TITLE));
        return response.isSuccess() ? response.getTitle() : null;
    }

    /**
     * Fetch chỉ description
     */
    public String fetchDescription(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.DESCRIPTION));
        return response.isSuccess() ? response.getDescription() : null;
    }

    /**
     * Fetch headings
     */
    public List<String> fetchHeadings(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.ALL_HEADINGS));
        return response.isSuccess() ? response.getAllHeadings() : List.of();
    }

    /**
     * Fetch images
     */
    public List<String> fetchImages(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.IMAGE_URLS));
        return response.isSuccess() ? response.getImageUrls() : List.of();
    }

    /**
     * Fetch links
     */
    public List<String> fetchLinks(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.LINK_URLS));
        return response.isSuccess() ? response.getLinkUrls() : List.of();
    }

    /**
     * Fetch external links
     */
    public List<String> fetchExternalLinks(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.EXTERNAL_LINKS));
        return response.isSuccess() ? response.getExternalLinks() : List.of();
    }

    /**
     * Fetch internal links
     */
    public List<String> fetchInternalLinks(String baseUrl) {
        MetadataResponse response = fetchMetadata(baseUrl, List.of(MetadataType.INTERNAL_LINKS));
        return response.isSuccess() ? response.getInternalLinks() : List.of();
    }
}
