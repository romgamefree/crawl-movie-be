package com.devteria.identityservice.service;

import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.helpers.DownloadHelper;
import com.devteria.identityservice.utils.FileUtils;
import com.devteria.identityservice.utils.M3U8Utils;
import com.devteria.identityservice.utils.RegexUtils;
import com.devteria.identityservice.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class M3U8DownloadService {

    private final DownloadHelper downloadHelper;

    private static final String BASE_SAVE_FOLDER_PLAYLIST = "../../../data/playlist";
    private static final String FOLDER_SEGMENTS = "segments";
    private static final String FILE_MASTER_M3U8 = "master.m3u8";
    private static final String FILE_INDEX_M3U8 = "index.m3u8";
    private static final int BATCH_SIZE = 10;
    private static final int MAX_CONCURRENT_DOWNLOADS = 20;
    private static final String MASTER_M3U8_URL_FORMAT_HOT_24HD = "https://hot.24playerhd.com/iosplaylist/%s/%s.m3u8";
    private static final String MASTER_M3U8_URL_FORMAT_MAIN_24HD = "https://main.24playerhd.com/newplaylist/%s/%s.m3u8";

    private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);

    /**
     * Tải và lưu video M3U8
     */
    public void downloadM3U8Video(String embedLink, String movieId, String episodeId, boolean isSingle) {
        try {
            // 1) Lấy ID
            String id = this.extractIdFromEmbedLink(embedLink);
            String masterM3U8UrlHot24PlayerHd = this.generateMasterM3U8UrlHot24PlayerHd(id);
            String masterM3U8UrlMain24PlayerHd = this.generateMasterM3U8UrlMain24PlayerHd(id);
            String masterM3u8Url;
            // 2) Xác định thư mục lưu
            String saveFolder = buildSaveFolder(movieId, episodeId, isSingle);
            FileUtils.createFolder(saveFolder); // đảm bảo tạo thư mục cha

            log.info("🚀 Bắt đầu tải video M3U8: {}", masterM3U8UrlHot24PlayerHd);
            log.info("📂 Lưu vào thư mục: {}", saveFolder);

            // 3) Tải master m3u8
            String masterContent = downloadHelper.downloadContent(masterM3U8UrlHot24PlayerHd).trim();

            if (!"404".equals(masterContent)) {
                masterM3u8Url = masterM3U8UrlHot24PlayerHd;
                masterContent = downloadHelper.downloadContent(masterM3U8UrlHot24PlayerHd);
            } else {
                masterM3u8Url = masterM3U8UrlMain24PlayerHd;
                masterContent = downloadHelper.downloadContent(masterM3U8UrlMain24PlayerHd);
            }

            // 4) Lấy danh sách m3u8 con (variant)
            List<String> variantPaths = M3U8Utils.getPathsFromM3U8(masterContent);

            // Mapping để rewrite master → local index theo label
            Map<String, String> variantPathToLabel = new HashMap<>();

            for (String variantPath : variantPaths) {
                String variantUrl = M3U8Utils.resolveUrl(masterM3u8Url, variantPath);

                String variantContent = downloadHelper.downloadContent(variantUrl);

                // Xác định chất lượng (label) ví dụ: 360p, 720p, 1080p
                String label = M3U8Utils.getQualityLabel(masterContent, variantPath);
                variantPathToLabel.put(variantPath, label);

                // Folder cho chất lượng
                String qualityFolder = saveFolder + "/" + label;
                FileUtils.createFolder(qualityFolder);
                FileUtils.createFolder(qualityFolder + "/" + FOLDER_SEGMENTS);

                // Lưu file index.m3u8 (đã rewrite sang local segments/)
                String localM3U8Path = qualityFolder + "/" + FILE_INDEX_M3U8;
                String localM3U8Content = M3U8Utils.rewriteM3U8ToLocal(variantContent, variantUrl,
                        FOLDER_SEGMENTS + "/");
                FileUtils.saveFileContent(localM3U8Path, localM3U8Content);
                log.info("✅ Đã lưu index.m3u8 cho chất lượng {} -> {}", label, localM3U8Path);

                // 5) Lấy danh sách segment
                List<String> segmentPaths = M3U8Utils.getPathsFromM3U8(variantContent);

                // Tải segment song song theo batch
                downloadSegmentsInBatches(segmentPaths, variantUrl, qualityFolder + "/" + FOLDER_SEGMENTS);
            }

            // 6) Ghi master.m3u8 đã rewrite sang local đường dẫn tới index.m3u8 theo label
            String masterRewritten = M3U8Utils.rewriteMasterToLocal(masterContent, variantPathToLabel);
            String masterLocalPath = saveFolder + "/" + FILE_MASTER_M3U8;
            FileUtils.saveFileContent(masterLocalPath, masterRewritten);

        } catch (Exception e) {
            log.error("❌ Lỗi khi tải video M3U8: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi tải video M3U8", e);
        }
    }

    /**
     * Build thư mục lưu dựa vào loại phim
     */
    private String buildSaveFolder(String movieId, String episodeId, boolean isSingle) {
        if (isSingle) {
            return BASE_SAVE_FOLDER_PLAYLIST + "/" + movieId;
        } else {
            return BASE_SAVE_FOLDER_PLAYLIST + "/" + movieId + "/" + episodeId;
        }
    }

    public String buildMasterLocalPath(String movieId, String episodeId, boolean isSingle) {
        String folder = buildSaveFolder(movieId, episodeId, isSingle);
        return folder + "/" + FILE_MASTER_M3U8;
    }

    /**
     * Tải segments theo batch
     */
    private void downloadSegmentsInBatches(List<String> segmentPaths, String variantUrl, String segmentFolder) {
        for (int i = 0; i < segmentPaths.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, segmentPaths.size());
            List<String> batch = segmentPaths.subList(i, endIndex);

            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(segPath -> {
                        String segUrl = M3U8Utils.resolveUrl(variantUrl, segPath);
                        String fileName = FileUtils.extractCleanFileName(segUrl);
                        String savePath = segmentFolder + "/" + fileName;

                        return CompletableFuture.runAsync(() -> {
                            downloadHelper.downloadAndSaveFile(segUrl, savePath);
                        }, executorService);
                    })
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }
    }

    public String extractIdFromEmbedLink(String embedLink) {
        if (StringUtils.isNullOrBlank(embedLink)) {
            throw new AppException(ErrorCode.DATA_IS_NOT_VALID);
        }

        if (embedLink.contains("24playerhd.com")) {
            return RegexUtils.extractIdFromEmbedLinkFromHot24PlayerHd(embedLink);
        } else if (embedLink.contains("stream1689.com")) {
            return RegexUtils.extractIdFromEmbedLinkFromStream1689(embedLink);
        } else {
            throw new AppException(ErrorCode.DOMAIN_NOT_SUPPORTED);
        }
    }

    public String generateMasterM3U8UrlHot24PlayerHd(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        return String.format(MASTER_M3U8_URL_FORMAT_HOT_24HD, id, id);
    }

    public String generateMasterM3U8UrlMain24PlayerHd(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID không được để trống");
        }
        return String.format(MASTER_M3U8_URL_FORMAT_MAIN_24HD, id, id);
    }
}
