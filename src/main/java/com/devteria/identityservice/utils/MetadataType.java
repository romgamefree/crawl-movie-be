package com.devteria.identityservice.utils;

import lombok.Getter;

@Getter
public enum MetadataType {

    // Basic metadata
    TITLE("title", "Trích xuất title từ thẻ <title>"),
    DESCRIPTION("description", "Trích xuất description từ meta[name=description] hoặc og:description"),
    KEYWORDS("keywords", "Trích xuất keywords từ meta[name=keywords]"),
    AUTHOR("author", "Trích xuất author từ meta[name=author] hoặc article:author"),

    // Open Graph
    OG_TITLE("og_title", "Trích xuất Open Graph title"),
    OG_DESCRIPTION("og_description", "Trích xuất Open Graph description"),
    OG_IMAGE("og_image", "Trích xuất Open Graph image"),
    OG_TYPE("og_type", "Trích xuất Open Graph type"),
    OG_URL("og_url", "Trích xuất Open Graph URL"),

    // Twitter Card
    TWITTER_CARD("twitter_card", "Trích xuất Twitter Card type"),
    TWITTER_TITLE("twitter_title", "Trích xuất Twitter Card title"),
    TWITTER_DESCRIPTION("twitter_description", "Trích xuất Twitter Card description"),
    TWITTER_IMAGE("twitter_image", "Trích xuất Twitter Card image"),

    // SEO & Technical
    CANONICAL_URL("canonical_url", "Trích xuất canonical URL"),
    ROBOTS("robots", "Trích xuất robots meta tag"),
    VIEWPORT("viewport", "Trích xuất viewport meta tag"),
    CHARSET("charset", "Trích xuất charset"),

    // Content
    H1("h1", "Trích xuất heading h1"),
    H2("h2", "Trích xuất heading h2"),
    H3("h3", "Trích xuất heading h3"),
    ALL_HEADINGS("all_headings", "Trích xuất tất cả headings (h1-h6)"),

    // Media
    IMAGE_ALT_TEXTS("image_alt_texts", "Trích xuất alt text của tất cả images"),
    IMAGE_URLS("image_urls", "Trích xuất URL của tất cả images"),
    VIDEO_URLS("video_urls", "Trích xuất URL của tất cả videos"),

    // Links
    LINK_TEXTS("link_texts", "Trích xuất text của tất cả links"),
    LINK_URLS("link_urls", "Trích xuất URL của tất cả links"),
    EXTERNAL_LINKS("external_links", "Trích xuất external links"),
    INTERNAL_LINKS("internal_links", "Trích xuất internal links"),

    // Structured Data
    STRUCTURED_DATA("structured_data", "Trích xuất structured data (JSON-LD)"),
    SCHEMA_ORG("schema_org", "Trích xuất Schema.org markup"),

    // Social Media
    FACEBOOK_APP_ID("facebook_app_id", "Trích xuất Facebook App ID"),
    TWITTER_SITE("twitter_site", "Trích xuất Twitter site username"),
    TWITTER_CREATOR("twitter_creator", "Trích xuất Twitter creator username");

    private final String key;
    private final String description;

    MetadataType(String key, String description) {
        this.key = key;
        this.description = description;
    }

    // Predefined groups for convenience
    public static final MetadataType[] BASIC_METADATA = { TITLE, DESCRIPTION, H1, H2 };
    public static final MetadataType[] SEO_METADATA = { TITLE, DESCRIPTION, KEYWORDS, CANONICAL_URL, ROBOTS };
    public static final MetadataType[] SOCIAL_METADATA = { OG_TITLE, OG_DESCRIPTION, OG_IMAGE, TWITTER_CARD,
            TWITTER_TITLE };
    public static final MetadataType[] CONTENT_METADATA = { H1, H2, H3, ALL_HEADINGS, IMAGE_ALT_TEXTS };
}
