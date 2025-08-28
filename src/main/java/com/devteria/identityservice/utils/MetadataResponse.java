package com.devteria.identityservice.utils;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@Accessors(chain = true)
public class MetadataResponse {

    // Request info
    private String url;
    private List<MetadataType> requestedTypes;

    // Basic metadata
    private String title;
    private String description;
    private String keywords;
    private String author;

    // Open Graph
    private String ogTitle;
    private String ogDescription;
    private String ogImage;
    private String ogType;
    private String ogUrl;

    // Twitter Card
    private String twitterCard;
    private String twitterTitle;
    private String twitterDescription;
    private String twitterImage;
    private String twitterSite;
    private String twitterCreator;

    // SEO & Technical
    private String canonicalUrl;
    private String robots;
    private String viewport;
    private String charset;

    // Content
    private String h1;
    private String h2;
    private String h3;
    private List<String> allHeadings;

    // Media
    private List<String> imageAltTexts;
    private List<String> imageUrls;
    private List<String> videoUrls;

    // Links
    private List<String> linkTexts;
    private List<String> linkUrls;
    private List<String> externalLinks;
    private List<String> internalLinks;

    // Structured Data
    private List<String> structuredData;
    private List<String> schemaOrg;

    // Social Media
    private String facebookAppId;

    // All metadata as map
    private Map<String, Object> metadataMap;

    // Performance & Status
    private long fetchTimeMs;
    private LocalDateTime fetchTime;
    private boolean success;
    private String errorMessage;
    private int httpStatusCode;

    // Helper methods
    public boolean hasTitle() {
        return title != null && !title.isBlank();
    }

    public boolean hasDescription() {
        return description != null && !description.isBlank();
    }

    public boolean hasH1() {
        return h1 != null && !h1.isBlank();
    }

    public boolean hasOgImage() {
        return ogImage != null && !ogImage.isBlank();
    }

    public String getBestTitle() {
        return ogTitle != null ? ogTitle : title;
    }

    public String getBestDescription() {
        return ogDescription != null ? ogDescription : description;
    }

    public String getBestImage() {
        return ogImage != null ? ogImage : (twitterImage != null ? twitterImage : null);
    }

    public boolean isSuccess() {
        return success && errorMessage == null;
    }

    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (hasTitle())
            sb.append("Title: ").append(getBestTitle()).append("\n");
        if (hasDescription())
            sb.append("Description: ").append(getBestDescription()).append("\n");
        if (hasH1())
            sb.append("H1: ").append(h1).append("\n");
        if (hasOgImage())
            sb.append("Image: ").append(getBestImage()).append("\n");
        return sb.toString().trim();
    }
}
