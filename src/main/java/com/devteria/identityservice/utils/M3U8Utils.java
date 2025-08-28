package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class M3U8Utils {

    /**
     * Lọc ra các dòng không bắt đầu bằng # từ M3U8 content
     */
    public static List<String> getPathsFromM3U8(String content) {
        return content.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
    }

    /**
     * Thay thế các URL segment trong m3u8 variant thành đường dẫn local dạng
     * segments/<filename>
     * Hỗ trợ cả URL tuyệt đối lẫn đường dẫn tương đối bằng cách resolve dựa trên
     * variantUrl.
     */
    public static String rewriteM3U8ToLocal(String content, String variantUrl, String segmentFolderPrefix) {
        final String prefix = segmentFolderPrefix.endsWith("/") ? segmentFolderPrefix : segmentFolderPrefix + "/";
        return content.lines()
                .map(line -> {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        return line;
                    }

                    String absolute;
                    try {
                        // Nếu là URL tuyệt đối thì dùng luôn, nếu không sẽ ném lỗi → bắt và resolve thủ
                        // công
                        URL tmp = new URL(trimmed);
                        absolute = tmp.toString();
                    } catch (Exception ignore) {
                        try {
                            absolute = resolveUrl(variantUrl, trimmed);
                        } catch (Exception e2) {
                            log.warn("⚠️ Không thể resolve segment {} với base {}: {}", trimmed, variantUrl,
                                    e2.getMessage());
                            return line;
                        }
                    }

                    try {
                        URL url = new URL(absolute);
                        String path = url.getPath();
                        String fileName = path.substring(path.lastIndexOf('/') + 1);
                        return prefix + fileName;
                    } catch (Exception e) {
                        log.warn("⚠️ Không thể parse URL {}: {}", absolute, e.getMessage());
                        return line;
                    }
                })
                .collect(Collectors.joining("\n"));
    }

    /**
     * Lấy label chất lượng từ master content
     */
    public static String getQualityLabel(String masterContent, String variantPath) {
        String[] lines = masterContent.split("\n");
        for (int i = 0; i < lines.length - 1; i++) {
            if (lines[i + 1].trim().equals(variantPath) && lines[i].startsWith("#EXT-X-STREAM-INF")) {
                Pattern pattern = Pattern.compile("RESOLUTION=(\\d+x\\d+)");
                Matcher matcher = pattern.matcher(lines[i]);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }
        return variantPath;
    }

    /**
     * Ghi đè nội dung master.m3u8 để các dòng trỏ đến index.m3u8 local theo label.
     * Mapping: variantPath (dòng trong master) → label
     */
    public static String rewriteMasterToLocal(String masterContent, java.util.Map<String, String> variantPathToLabel) {
        String[] lines = masterContent.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#") && variantPathToLabel.containsKey(trimmed)) {
                String label = variantPathToLabel.get(trimmed);
                sb.append(label).append("/index.m3u8");
            } else {
                sb.append(line);
            }
            sb.append('\n');
        }
        return sb.toString().trim();
    }

    /**
     * Chuyển URL thành đường dẫn local
     */
    public static String urlToLocalPath(String fileUrl, String baseSaveFolder) {
        try {
            URL url = new URL(fileUrl);
            String pathname = url.getPath().replaceAll("^/+", "");
            return Paths.get(baseSaveFolder, pathname).toString();
        } catch (Exception e) {
            log.error("❌ Lỗi khi chuyển URL thành local path {}: {}", fileUrl, e.getMessage());
            throw new RuntimeException("Failed to convert URL to local path", e);
        }
    }

    /**
     * Lấy URL tuyệt đối từ base URL và relative path
     */
    public static String resolveUrl(String base, String relative) {
        try {
            URL baseUrl = new URL(base);
            URL resolvedUrl = new URL(baseUrl, relative);
            return resolvedUrl.toString();
        } catch (Exception e) {
            log.error("❌ Lỗi khi resolve URL {} với base {}: {}", relative, base, e.getMessage());
            throw new RuntimeException("Failed to resolve URL", e);
        }
    }
}
