package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MetadataUtils {

    /**
     * Trích xuất metadata theo danh sách types được chỉ định
     */
    public static MetadataResponse extractMetadata(Document doc, String url, List<MetadataType> types) {
        long startTime = System.currentTimeMillis();

        try {
            MetadataResponse.MetadataResponseBuilder builder = MetadataResponse.builder()
                    .url(url)
                    .requestedTypes(types)
                    .fetchTime(LocalDateTime.now())
                    .success(true);

            Map<String, Object> metadataMap = new HashMap<>();

            for (MetadataType type : types) {
                Object value = extractByType(doc, type, url);
                if (value != null) {
                    metadataMap.put(type.getKey(), value);
                    setBuilderValue(builder, type, value);
                }
            }

            builder.metadataMap(metadataMap);
            builder.fetchTimeMs(System.currentTimeMillis() - startTime);

            return builder.build();

        } catch (Exception e) {
            log.error("❌ Lỗi khi extract metadata: {}", e.getMessage(), e);
            return MetadataResponse.builder()
                    .url(url)
                    .requestedTypes(types)
                    .success(false)
                    .errorMessage(e.getMessage())
                    .fetchTime(LocalDateTime.now())
                    .fetchTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    /**
     * Trích xuất tất cả metadata có sẵn
     */
    public static MetadataResponse extractAllMetadata(Document doc, String url) {
        return extractMetadata(doc, url, Arrays.asList(MetadataType.values()));
    }

    /**
     * Trích xuất metadata cơ bản
     */
    public static MetadataResponse extractBasicMetadata(Document doc, String url) {
        return extractMetadata(doc, url, Arrays.asList(MetadataType.BASIC_METADATA));
    }

    /**
     * Trích xuất SEO metadata
     */
    public static MetadataResponse extractSeoMetadata(Document doc, String url) {
        return extractMetadata(doc, url, Arrays.asList(MetadataType.SEO_METADATA));
    }

    /**
     * Trích xuất social media metadata
     */
    public static MetadataResponse extractSocialMetadata(Document doc, String url) {
        return extractMetadata(doc, url, Arrays.asList(MetadataType.SOCIAL_METADATA));
    }

    /**
     * Trích xuất content metadata
     */
    public static MetadataResponse extractContentMetadata(Document doc, String url) {
        return extractMetadata(doc, url, Arrays.asList(MetadataType.CONTENT_METADATA));
    }

    /**
     * Trích xuất metadata theo type
     */
    private static Object extractByType(Document doc, MetadataType type, String url) {
        if (doc == null)
            return null;

        switch (type) {
            case TITLE:
                return extractTitle(doc);
            case DESCRIPTION:
                return extractDescription(doc);
            case KEYWORDS:
                return extractKeywords(doc);
            case AUTHOR:
                return extractAuthor(doc);
            case OG_TITLE:
                return extractOgTitle(doc);
            case OG_DESCRIPTION:
                return extractOgDescription(doc);
            case OG_IMAGE:
                return extractOgImage(doc);
            case OG_TYPE:
                return extractOgType(doc);
            case OG_URL:
                return extractOgUrl(doc);
            case TWITTER_CARD:
                return extractTwitterCard(doc);
            case TWITTER_TITLE:
                return extractTwitterTitle(doc);
            case TWITTER_DESCRIPTION:
                return extractTwitterDescription(doc);
            case TWITTER_IMAGE:
                return extractTwitterImage(doc);
            case TWITTER_SITE:
                return extractTwitterSite(doc);
            case TWITTER_CREATOR:
                return extractTwitterCreator(doc);
            case CANONICAL_URL:
                return extractCanonicalUrl(doc);
            case ROBOTS:
                return extractRobots(doc);
            case VIEWPORT:
                return extractViewport(doc);
            case CHARSET:
                return extractCharset(doc);
            case H1:
                return extractH1(doc);
            case H2:
                return extractH2(doc);
            case H3:
                return extractH3(doc);
            case ALL_HEADINGS:
                return extractAllHeadings(doc);
            case IMAGE_ALT_TEXTS:
                return extractImageAltTexts(doc);
            case IMAGE_URLS:
                return extractImageUrls(doc);
            case VIDEO_URLS:
                return extractVideoUrls(doc);
            case LINK_TEXTS:
                return extractLinkTexts(doc);
            case LINK_URLS:
                return extractLinkUrls(doc);
            case EXTERNAL_LINKS:
                return extractExternalLinks(doc, url);
            case INTERNAL_LINKS:
                return extractInternalLinks(doc, url);
            case STRUCTURED_DATA:
                return extractStructuredData(doc);
            case SCHEMA_ORG:
                return extractSchemaOrg(doc);
            case FACEBOOK_APP_ID:
                return extractFacebookAppId(doc);
            default:
                return null;
        }
    }

    /**
     * Set value cho builder theo type
     */
    private static void setBuilderValue(MetadataResponse.MetadataResponseBuilder builder, MetadataType type,
            Object value) {
        switch (type) {
            case TITLE:
                builder.title((String) value);
                break;
            case DESCRIPTION:
                builder.description((String) value);
                break;
            case KEYWORDS:
                builder.keywords((String) value);
                break;
            case AUTHOR:
                builder.author((String) value);
                break;
            case OG_TITLE:
                builder.ogTitle((String) value);
                break;
            case OG_DESCRIPTION:
                builder.ogDescription((String) value);
                break;
            case OG_IMAGE:
                builder.ogImage((String) value);
                break;
            case OG_TYPE:
                builder.ogType((String) value);
                break;
            case OG_URL:
                builder.ogUrl((String) value);
                break;
            case TWITTER_CARD:
                builder.twitterCard((String) value);
                break;
            case TWITTER_TITLE:
                builder.twitterTitle((String) value);
                break;
            case TWITTER_DESCRIPTION:
                builder.twitterDescription((String) value);
                break;
            case TWITTER_IMAGE:
                builder.twitterImage((String) value);
                break;
            case TWITTER_SITE:
                builder.twitterSite((String) value);
                break;
            case TWITTER_CREATOR:
                builder.twitterCreator((String) value);
                break;
            case CANONICAL_URL:
                builder.canonicalUrl((String) value);
                break;
            case ROBOTS:
                builder.robots((String) value);
                break;
            case VIEWPORT:
                builder.viewport((String) value);
                break;
            case CHARSET:
                builder.charset((String) value);
                break;
            case H1:
                builder.h1((String) value);
                break;
            case H2:
                builder.h2((String) value);
                break;
            case H3:
                builder.h3((String) value);
                break;
            case ALL_HEADINGS:
                builder.allHeadings((List<String>) value);
                break;
            case IMAGE_ALT_TEXTS:
                builder.imageAltTexts((List<String>) value);
                break;
            case IMAGE_URLS:
                builder.imageUrls((List<String>) value);
                break;
            case VIDEO_URLS:
                builder.videoUrls((List<String>) value);
                break;
            case LINK_TEXTS:
                builder.linkTexts((List<String>) value);
                break;
            case LINK_URLS:
                builder.linkUrls((List<String>) value);
                break;
            case EXTERNAL_LINKS:
                builder.externalLinks((List<String>) value);
                break;
            case INTERNAL_LINKS:
                builder.internalLinks((List<String>) value);
                break;
            case STRUCTURED_DATA:
                builder.structuredData((List<String>) value);
                break;
            case SCHEMA_ORG:
                builder.schemaOrg((List<String>) value);
                break;
            case FACEBOOK_APP_ID:
                builder.facebookAppId((String) value);
                break;
        }
    }

    // Extraction methods
    public static String extractTitle(Document doc) {
        return doc != null ? doc.title() : null;
    }

    public static String extractDescription(Document doc) {
        if (doc == null)
            return null;

        // Thử meta[name=description] trước
        Element meta = doc.selectFirst("meta[name=description]");
        if (meta != null) {
            String content = meta.attr("content");
            if (!StringUtils.isNullOrBlank(content)) {
                return content;
            }
        }

        // Thử meta[property=og:description]
        meta = doc.selectFirst("meta[property=og:description]");
        if (meta != null) {
            String content = meta.attr("content");
            if (!StringUtils.isNullOrBlank(content)) {
                return content;
            }
        }

        return null;
    }

    public static String extractKeywords(Document doc) {
        return extractMetaContent(doc, "meta[name=keywords]");
    }

    public static String extractAuthor(Document doc) {
        if (doc == null)
            return null;

        // Thử meta[name=author]
        Element meta = doc.selectFirst("meta[name=author]");
        if (meta != null) {
            String content = meta.attr("content");
            if (!StringUtils.isNullOrBlank(content)) {
                return content;
            }
        }

        // Thử meta[property=article:author]
        meta = doc.selectFirst("meta[property=article:author]");
        if (meta != null) {
            String content = meta.attr("content");
            if (!StringUtils.isNullOrBlank(content)) {
                return content;
            }
        }

        return null;
    }

    public static String extractOgTitle(Document doc) {
        return extractMetaContent(doc, "meta[property=og:title]");
    }

    public static String extractOgDescription(Document doc) {
        return extractMetaContent(doc, "meta[property=og:description]");
    }

    public static String extractOgImage(Document doc) {
        return extractMetaContent(doc, "meta[property=og:image]");
    }

    public static String extractOgType(Document doc) {
        return extractMetaContent(doc, "meta[property=og:type]");
    }

    public static String extractOgUrl(Document doc) {
        return extractMetaContent(doc, "meta[property=og:url]");
    }

    public static String extractTwitterCard(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:card]");
    }

    public static String extractTwitterTitle(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:title]");
    }

    public static String extractTwitterDescription(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:description]");
    }

    public static String extractTwitterImage(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:image]");
    }

    public static String extractTwitterSite(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:site]");
    }

    public static String extractTwitterCreator(Document doc) {
        return extractMetaContent(doc, "meta[name=twitter:creator]");
    }

    public static String extractCanonicalUrl(Document doc) {
        if (doc == null)
            return null;

        Element link = doc.selectFirst("link[rel=canonical]");
        if (link != null) {
            String href = link.attr("href");
            if (!StringUtils.isNullOrBlank(href)) {
                return href;
            }
        }

        return null;
    }

    public static String extractRobots(Document doc) {
        return extractMetaContent(doc, "meta[name=robots]");
    }

    public static String extractViewport(Document doc) {
        return extractMetaContent(doc, "meta[name=viewport]");
    }

    public static String extractCharset(Document doc) {
        if (doc == null)
            return null;

        Element meta = doc.selectFirst("meta[charset]");
        if (meta != null) {
            return meta.attr("charset");
        }

        meta = doc.selectFirst("meta[http-equiv=Content-Type]");
        if (meta != null) {
            String content = meta.attr("content");
            if (content != null && content.contains("charset=")) {
                return content.split("charset=")[1];
            }
        }

        return null;
    }

    public static List<String> extractAllHeadings(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("h1, h2, h3, h4, h5, h6")
                .stream()
                .map(Element::text)
                .filter(text -> !StringUtils.isNullOrBlank(text))
                .collect(Collectors.toList());
    }

    public static String extractH1(Document doc) {
        return extractHeading(doc, "h1");
    }

    public static String extractH2(Document doc) {
        return extractHeading(doc, "h2");
    }

    public static String extractH3(Document doc) {
        return extractHeading(doc, "h3");
    }

    public static List<String> extractImageAltTexts(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("img[alt]")
                .stream()
                .map(img -> img.attr("alt"))
                .filter(alt -> !StringUtils.isNullOrBlank(alt))
                .collect(Collectors.toList());
    }

    public static List<String> extractImageUrls(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("img[src]")
                .stream()
                .map(img -> img.attr("src"))
                .filter(src -> !StringUtils.isNullOrBlank(src))
                .collect(Collectors.toList());
    }

    public static List<String> extractVideoUrls(Document doc) {
        if (doc == null)
            return List.of();

        List<String> videoUrls = new ArrayList<>();

        // Video tags
        videoUrls.addAll(doc.select("video[src]")
                .stream()
                .map(video -> video.attr("src"))
                .filter(src -> !StringUtils.isNullOrBlank(src))
                .collect(Collectors.toList()));

        // Source tags inside video
        videoUrls.addAll(doc.select("video source[src]")
                .stream()
                .map(source -> source.attr("src"))
                .filter(src -> !StringUtils.isNullOrBlank(src))
                .collect(Collectors.toList()));

        return videoUrls;
    }

    public static List<String> extractLinkTexts(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("a")
                .stream()
                .map(Element::text)
                .filter(text -> !StringUtils.isNullOrBlank(text))
                .collect(Collectors.toList());
    }

    public static List<String> extractLinkUrls(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("a[href]")
                .stream()
                .map(link -> link.attr("href"))
                .filter(href -> !StringUtils.isNullOrBlank(href))
                .collect(Collectors.toList());
    }

    public static List<String> extractExternalLinks(Document doc, String baseUrl) {
        if (doc == null || StringUtils.isNullOrBlank(baseUrl))
            return List.of();

        String baseDomain = extractDomain(baseUrl);

        return doc.select("a[href^=http]")
                .stream()
                .map(link -> link.attr("href"))
                .filter(href -> !StringUtils.isNullOrBlank(href))
                .filter(href -> !extractDomain(href).equals(baseDomain))
                .collect(Collectors.toList());
    }

    public static List<String> extractInternalLinks(Document doc, String baseUrl) {
        if (doc == null || StringUtils.isNullOrBlank(baseUrl))
            return List.of();

        String baseDomain = extractDomain(baseUrl);

        return doc.select("a[href^=http]")
                .stream()
                .map(link -> link.attr("href"))
                .filter(href -> !StringUtils.isNullOrBlank(href))
                .filter(href -> extractDomain(href).equals(baseDomain))
                .collect(Collectors.toList());
    }

    public static List<String> extractStructuredData(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("script[type=application/ld+json]")
                .stream()
                .map(Element::data)
                .filter(data -> !StringUtils.isNullOrBlank(data))
                .collect(Collectors.toList());
    }

    public static List<String> extractSchemaOrg(Document doc) {
        if (doc == null)
            return List.of();

        return doc.select("[itemtype]")
                .stream()
                .map(element -> element.attr("itemtype"))
                .filter(itemtype -> !StringUtils.isNullOrBlank(itemtype))
                .collect(Collectors.toList());
    }

    public static String extractFacebookAppId(Document doc) {
        return extractMetaContent(doc, "meta[property=fb:app_id]");
    }

    // Helper methods
    private static String extractMetaContent(Document doc, String selector) {
        if (doc == null)
            return null;

        Element meta = doc.selectFirst(selector);
        if (meta != null) {
            String content = meta.attr("content");
            if (!StringUtils.isNullOrBlank(content)) {
                return content;
            }
        }

        return null;
    }

    private static String extractHeading(Document doc, String tag) {
        if (doc == null)
            return null;

        Element heading = doc.selectFirst(tag);
        if (heading != null) {
            String text = heading.text();
            if (!StringUtils.isNullOrBlank(text)) {
                return text;
            }
        }

        return null;
    }

    private static String extractDomain(String url) {
        try {
            return new java.net.URL(url).getHost();
        } catch (Exception e) {
            return "";
        }
    }
}
