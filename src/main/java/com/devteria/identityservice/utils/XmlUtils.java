package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class XmlUtils {

    /**
     * Parse XML string thành Document
     */
    public static Document parseXmlDocument(String xmlContent) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Security: Disable external entity processing
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

        } catch (Exception e) {
            log.error("Lỗi khi parse XML document", e);
            return null;
        }
    }

    /**
     * Kiểm tra xem XML content có phải là sitemap index không
     */
    public static boolean isSitemapIndex(String xmlContent) {
        return xmlContent.contains("<sitemapindex") || xmlContent.contains("<sitemap>");
    }

    /**
     * Trích xuất URLs từ sitemap XML
     */
    public static List<String> extractUrlsFromSitemap(String sitemapContent) {
        List<String> urls = new ArrayList<>();

        try {
            Document document = parseXmlDocument(sitemapContent);
            if (document == null) return urls;

            Element root = document.getDocumentElement();
            if (root == null) return urls;

            String rootName = root.getNodeName();

            if ("urlset".equals(rootName)) {
                // sitemap kiểu chứa danh sách URL
                NodeList urlNodes = document.getElementsByTagName("url");
                for (int i = 0; i < urlNodes.getLength(); i++) {
                    Element urlElement = (Element) urlNodes.item(i);
                    NodeList locNodes = urlElement.getElementsByTagName("loc");
                    if (locNodes.getLength() > 0) {
                        String url = locNodes.item(0).getTextContent().trim();
                        if (!url.isEmpty()) urls.add(url);
                    }
                }
            } else if ("sitemapindex".equals(rootName)) {
                // sitemap kiểu chứa danh sách sitemap con
                NodeList sitemapNodes = document.getElementsByTagName("sitemap");
                for (int i = 0; i < sitemapNodes.getLength(); i++) {
                    Element sitemapElement = (Element) sitemapNodes.item(i);
                    NodeList locNodes = sitemapElement.getElementsByTagName("loc");
                    if (locNodes.getLength() > 0) {
                        String url = locNodes.item(0).getTextContent().trim();
                        if (!url.isEmpty()) urls.add(url);
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi parse sitemap content", e);
        }

        return urls;
    }


    /**
     * Trích xuất sub-sitemap URLs từ sitemap index
     */
    public static List<String> extractSubSitemapUrls(String sitemapContent) {
        List<String> subSitemapUrls = new ArrayList<>();

        try {
            Document document = parseXmlDocument(sitemapContent);
            if (document == null)
                return subSitemapUrls;

            NodeList sitemapNodes = document.getElementsByTagName("sitemap");

            for (int i = 0; i < sitemapNodes.getLength(); i++) {
                Element sitemapElement = (Element) sitemapNodes.item(i);
                NodeList locNodes = sitemapElement.getElementsByTagName("loc");

                if (locNodes.getLength() > 0) {
                    String url = locNodes.item(0).getTextContent().trim();
                    if (!url.isEmpty()) {
                        subSitemapUrls.add(url);
                        log.debug("🔗 Tìm thấy sub-sitemap: {}", url);
                    }
                }
            }

        } catch (Exception e) {
            log.error("❌ Lỗi khi parse sitemap index", e);
        }

        return subSitemapUrls;
    }
}
