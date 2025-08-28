package com.devteria.identityservice.service;

import com.devteria.identityservice.configuration.M3u8Properties;
import com.devteria.identityservice.constant.MovieStatus;
import com.devteria.identityservice.constant.MovieType;
import com.devteria.identityservice.constant.SelectorMovieDetail;
import com.devteria.identityservice.dto.request.CrawlSourceRequest;
import com.devteria.identityservice.dto.response.BulkInsertResponse;
import com.devteria.identityservice.dto.response.CrawlSourceResponse;
import com.devteria.identityservice.dto.response.EpisodeInfo;
import com.devteria.identityservice.dto.response.MovieResponse;
import com.devteria.identityservice.entity.*;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.helpers.HtmlFetcherHelper;
import com.devteria.identityservice.helpers.MetadataHelper;
import com.devteria.identityservice.helpers.SeleniumHelper;
import com.devteria.identityservice.mapper.CrawlSourceMapper;
import com.devteria.identityservice.mapper.MovieMapper;
import com.devteria.identityservice.repository.*;
import com.devteria.identityservice.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.time.Duration;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CrawlSourceService {
    // mappers
    CrawlSourceMapper mapper;
    MovieMapper movieMapper;

    // repositories
    CrawlSourceRepository repository;
    SelectorRepository selectorRepository;
    MovieRepository movieRepository;
    ActorRepository actorRepository;
    CategoryRepository categoryRepository;
    CountryRepository countryRepository;
    DirectorRepository directorRepository;
    EpisodeRepository episodeRepository;
    ServerDataRepository serverDataRepository;

    // helpers
    HtmlFetcherHelper htmlFetcherHelper;
    MetadataHelper metadataHelper;
    SeleniumHelper seleniumHelper;
    VideoLinkExtractor videoLinkExtractor;
    // services
    M3U8DownloadService m3U8DownloadService;
    M3u8Properties m3u8Properties;

    public CrawlSourceResponse create(CrawlSourceRequest request) {
        CrawlSource entity = mapper.toEntity(request);

        // Set Selector nếu có selectorId
        if (request.getSelectorId() != null) {
            entity.setSelector(selectorRepository.findById(request.getSelectorId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
        }

        if (entity.getBaseUrl() != null) {
            String info = metadataHelper.fetchTitleAndDescription(entity.getBaseUrl());
            if (info != null && !info.isBlank()) {
                entity.setNote(info);
            }
        }
        return mapper.toResponse(repository.save(entity));
    }

    public CrawlSourceResponse update(String id, CrawlSourceRequest request) {
        CrawlSource entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);

        // Set Selector nếu có selectorId
        if (request.getSelectorId() != null) {
            entity.setSelector(selectorRepository.findById(request.getSelectorId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
        } else {
            entity.setSelector(null);
        }

        if (entity.getBaseUrl() != null) {
            String info = metadataHelper.fetchTitleAndDescription(entity.getBaseUrl());
            if (info != null && !info.isBlank()) {
                entity.setNote(info);
            }
        }
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public CrawlSourceResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<CrawlSourceResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Transactional
    public MovieResponse insertFromCrawlSource(String crawlSourceId, boolean force) {
        CrawlSource crawlSource = repository.findById(crawlSourceId)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

        // Kiểm tra xem đã được insert chưa
        if (crawlSource.getInserted()) {
            if (force) {
                log.warn("↻ Bỏ qua kiểm tra 'inserted' và chạy lại theo yêu cầu cho: {}", crawlSource.getBaseUrl());
            } else {
                log.warn("⚠️ Crawl source đã được insert trước đó: {}", crawlSource.getBaseUrl());
                throw new AppException(ErrorCode.DATA_ALREADY_EXISTED);
            }
        }

        Selector selector = crawlSource.getSelector();
        if (selector == null) {
            log.error("❌ Selector not found");
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
        }
        Set<SelectorItem> selectorItems = selector.getSelectorItems();
        if (selectorItems == null || selectorItems.isEmpty()) {
            log.error("❌ Selector items not found");
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
        }

        String baseUrl = crawlSource.getBaseUrl();

        log.info("🚀 Bắt đầu crawl dữ liệu từ URL: {}", baseUrl);

        try {
            // Luôn render bằng JavaScript để lấy dữ liệu đầy đủ
            log.info("⚡ Luôn render bằng JavaScript để lấy dữ liệu đầy đủ");
            String jsHtml = htmlFetcherHelper.fetchUrlWithJs(baseUrl);
            Document finalDoc;
            Map<String, String> extractedData;

            if (jsHtml != null && !jsHtml.isBlank()) {
                Document jsDoc = Jsoup.parse(jsHtml, baseUrl);
                Map<String, String> jsExtractedData = extractDataFromSelectors(jsDoc, selectorItems, baseUrl);
                extractedData = new HashMap<>(jsExtractedData);
                finalDoc = jsDoc;
                log.info("✅ Đã extract dữ liệu từ JavaScript rendering");

                // Không fallback: yêu cầu luôn render JS
            } else {
                // Không render được JS -> coi là lỗi
                log.error("❌ Không thể render JavaScript cho URL: {}", baseUrl);
                throw new AppException(ErrorCode.CAN_NOT_RENDER_HTML_WITH_JS);
            }

            if (extractedData.isEmpty()) {
                log.warn("⚠️ Không tìm thấy dữ liệu nào từ selectors");
                throw new AppException(ErrorCode.DATA_NOT_FOUND);
            }

            log.info("📊 Dữ liệu đã extract: {}", extractedData);

            // Lấy selector cho TITLE
            String titleQuery = null;
            String titleAttribute = null;
            for (SelectorItem item : selectorItems) {
                if (SelectorMovieDetail.TITLE.getValue().equals(item.getName())) {
                    titleQuery = item.getQuery();
                    titleAttribute = item.getAttribute();
                    break;
                }
            }

            // Tạo hoặc tìm Movie
            Movie movie = findOrCreateMovie(extractedData, finalDoc, baseUrl, titleQuery, titleAttribute);

            // Lưu các entity liên quan
            saveRelatedEntities(movie, extractedData, baseUrl);

            // Đánh dấu đã insert thành công
            crawlSource.setInserted(true);
            repository.save(crawlSource);

            log.info("✅ Hoàn thành crawl và lưu dữ liệu cho movie: {}", movie.getTitle());

            return movieMapper.toResponse(movie);

        } catch (Exception e) {
            log.error("❌ Lỗi khi crawl dữ liệu từ URL {}: {}", baseUrl, e.getMessage(), e);
            throw new AppException(ErrorCode.DATA_NOT_FOUND);
        }
    }

    /**
     * Insert dữ liệu từ danh sách CrawlSource IDs cụ thể (chỉ những cái chưa được
     * insert)
     * 
     * @param crawlSourceIds Danh sách IDs của crawl sources cần xử lý
     * @return Thông tin chi tiết về quá trình insert
     */
    @Transactional
    public BulkInsertResponse insertFromCrawlSourceIds(List<String> crawlSourceIds, boolean force) {
        long startTime = System.currentTimeMillis();
        log.info("🚀 Bắt đầu insert từ {} crawl source IDs", crawlSourceIds.size());

        int successCount = 0;
        int errorCount = 0;
        int skippedCount = 0;
        List<String> errorUrls = new ArrayList<>();
        List<String> skippedUrls = new ArrayList<>();

        for (int i = 0; i < crawlSourceIds.size(); i++) {
            String crawlSourceId = crawlSourceIds.get(i);

            try {
                // Kiểm tra xem crawl source đã được insert chưa
                CrawlSource crawlSource = repository.findById(crawlSourceId)
                        .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));

                if (!force && crawlSource.getInserted()) {
                    skippedCount++;
                    skippedUrls.add(crawlSource.getBaseUrl());
                    log.info("⏭️ Bỏ qua crawl source đã được insert: {}", crawlSource.getBaseUrl());
                    continue;
                }

                log.info("📝 Đang xử lý crawl source {}/{}: ID {}", i + 1, crawlSourceIds.size(), crawlSourceId);

                MovieResponse movieResponse = insertFromCrawlSource(crawlSourceId, force);
                successCount++;

                // Đánh dấu đã insert thành công
                crawlSource.setInserted(true);
                repository.save(crawlSource);

                log.info("✅ Thành công: ID {} -> {}", crawlSourceId, movieResponse.getTitle());

            } catch (Exception e) {
                errorCount++;
                errorUrls.add("ID: " + crawlSourceId + " - " + e.getMessage());
                log.error("❌ Lỗi khi xử lý crawl source ID {}: {}", crawlSourceId, e.getMessage());
            }

            // Log progress mỗi 10 records
            if ((i + 1) % 10 == 0) {
                log.info("📊 Tiến độ: {}/{} (thành công: {}, lỗi: {}, bỏ qua: {})",
                        i + 1, crawlSourceIds.size(), successCount, errorCount, skippedCount);
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;

        log.info("🎉 Hoàn thành! Tổng kết: {}/{} thành công, {} lỗi, {} bỏ qua trong {}ms",
                successCount, crawlSourceIds.size(), errorCount, skippedCount, processingTime);

        if (!errorUrls.isEmpty()) {
            log.warn("⚠️ Các crawl source bị lỗi: {}", errorUrls);
        }

        if (!skippedUrls.isEmpty()) {
            log.info("ℹ️ Các crawl source đã được insert trước đó: {}", skippedUrls);
        }

        return BulkInsertResponse.builder()
                .totalSources(crawlSourceIds.size())
                .successCount(successCount)
                .errorCount(errorCount)
                .errorUrls(errorUrls)
                .message(String.format("Đã xử lý %d crawl sources: %d thành công, %d lỗi, %d bỏ qua trong %dms",
                        crawlSourceIds.size(), successCount, errorCount, skippedCount, processingTime))
                .processingTimeMs(processingTime)
                .build();
    }

    /**
     * Insert tất cả movies từ các crawl sources có enabled = true và inserted =
     * false
     * 
     * @return Thông tin chi tiết về quá trình insert
     */
    @Transactional
    public BulkInsertResponse insertAllMovies() {
        long startTime = System.currentTimeMillis();
        log.info("🚀 Bắt đầu insert tất cả movies từ crawl sources enabled và chưa insert");

        // Lấy trực tiếp Set<String> IDs thay vì toàn bộ entity
        Set<String> crawlSourceIds = repository.findEnabledAndNotInsertedIds();

        if (crawlSourceIds.isEmpty()) {
            log.info("ℹ️ Không có crawl source nào cần xử lý (tất cả đã được insert hoặc bị disable)");
            return BulkInsertResponse.builder()
                    .totalSources(0)
                    .successCount(0)
                    .errorCount(0)
                    .errorUrls(new ArrayList<>())
                    .message("Không có crawl source nào cần xử lý")
                    .processingTimeMs(0)
                    .build();
        }

        log.info("📊 Tìm thấy {} crawl sources cần xử lý", crawlSourceIds.size());

        // Gọi method insertFromCrawlSourceIds để xử lý (force = false để bỏ qua
        // inserted)
        BulkInsertResponse response = insertFromCrawlSourceIds(new ArrayList<>(crawlSourceIds), false);

        long totalProcessingTime = System.currentTimeMillis() - startTime;

        // Cập nhật thời gian xử lý tổng cộng
        response.setProcessingTimeMs(totalProcessingTime);

        log.info("🎉 Hoàn thành insert tất cả movies trong {}ms", totalProcessingTime);

        return response;
    }

    /**
     * Extract dữ liệu từ HTML document sử dụng các selector items
     */
    private Map<String, String> extractDataFromSelectors(Document doc, Set<SelectorItem> selectorItems,
            String baseUrl) {
        Map<String, String> extractedData = new HashMap<>();

        for (SelectorItem selectorItem : selectorItems) {
            String name = selectorItem.getName();
            String query = selectorItem.getQuery();
            String attribute = selectorItem.getAttribute();
            Boolean isList = selectorItem.getIsList();

            try {
                String value = extractValueFromSelector(doc, query, attribute, isList, baseUrl);
                if (value != null && !value.trim().isEmpty()) {
                    extractedData.put(name, value.trim());
                    log.debug("✅ Extract thành công {}: {}", name, value);
                } else {
                    log.warn("⚠️ Không tìm thấy dữ liệu cho selector: {}", name);
                }
            } catch (Exception e) {
                log.warn("⚠️ Lỗi khi extract selector {}: {}", name, e.getMessage());
            }
        }

        // todo: check again Xử lý thủ công cho các trường đặc biệt nếu chưa có dữ liệu
        processManualExtraction(doc, extractedData);

        return extractedData;
    }

    /**
     * Xử lý thủ công cho các trường đặc biệt
     */
    private void processManualExtraction(Document doc, Map<String, String> extractedData) {
        // Tìm tất cả các thẻ p trong .movie-detail
        Elements movieDetailParagraphs = doc.select(".movie-detail p");

        for (Element p : movieDetailParagraphs) {
            String text = p.text();

            // Kiểm tra và xử lý Directors
            if (!extractedData.containsKey(SelectorMovieDetail.DIRECTORS.getValue()) && text.contains("Director:")) {
                String directorsValue = extractTextFromLinks(p.html());
                if (directorsValue != null && !directorsValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.DIRECTORS.getValue(), directorsValue.trim());
                    log.debug("✅ Extract thủ công directors: {}", directorsValue);
                }
            }

            // Kiểm tra và xử lý Actors
            if (!extractedData.containsKey(SelectorMovieDetail.ACTORS.getValue()) && text.contains("Actors:")) {
                String actorsValue = extractTextFromLinks(p.html());
                if (actorsValue != null && !actorsValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.ACTORS.getValue(), actorsValue.trim());
                    log.debug("✅ Extract thủ công actors: {}", actorsValue);
                }
            }

            // Kiểm tra và xử lý Countries
            if (!extractedData.containsKey(SelectorMovieDetail.COUNTRIES.getValue()) && text.contains("Country:")) {
                String countriesValue = extractTextFromLinks(p.html());
                if (countriesValue != null && !countriesValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.COUNTRIES.getValue(), countriesValue.trim());
                    log.debug("✅ Extract thủ công countries: {}", countriesValue);
                }
            }

            // Kiểm tra và xử lý Categories
            if (!extractedData.containsKey(SelectorMovieDetail.CATEGORY.getValue()) && text.contains("Genres:")) {
                String categoryValue = extractTextFromLinks(p.html());
                if (categoryValue != null && !categoryValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.CATEGORY.getValue(), categoryValue.trim());
                    log.debug("✅ Extract thủ công category: {}", categoryValue);
                }
            }

            // Kiểm tra và xử lý Trailer
            if (!extractedData.containsKey(SelectorMovieDetail.TRAILER.getValue()) && text.contains("Trailer:")) {
                String trailerValue = extractHrefFromLinks(p.html());
                if (trailerValue != null && !trailerValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.TRAILER.getValue(), trailerValue.trim());
                    log.debug("✅ Extract thủ công trailer: {}", trailerValue);
                }
            }
        }

        // Xử lý Release Year
        if (!extractedData.containsKey(SelectorMovieDetail.RELEASE_YEAR.getValue())) {
            Elements releaseElements = doc.select(".movie-detail span.title-year");
            if (!releaseElements.isEmpty()) {
                String releaseText = releaseElements.first().text();
                String yearValue = extractYearFromText(releaseText);
                if (yearValue != null && !yearValue.trim().isEmpty()) {
                    extractedData.put(SelectorMovieDetail.RELEASE_YEAR.getValue(), yearValue.trim());
                    log.debug("✅ Extract thủ công releaseYear: {}", yearValue);
                }
            }
        }
    }

    /**
     * Trích xuất href từ các thẻ <a> trong HTML
     */
    private String extractHrefFromLinks(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank())
            return htmlContent;

        // Tìm href attribute trong thẻ <a>
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("href=[\"']([^\"']+)[\"']");
        java.util.regex.Matcher matcher = pattern.matcher(htmlContent);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Trích xuất năm từ text
     */
    private String extractYearFromText(String text) {
        if (text == null || text.isBlank())
            return null;

        // Tìm năm trong text (4 chữ số liên tiếp)
        Pattern pattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }

        return null;
    }

    /**
     * Trích xuất text từ các thẻ <a> trong HTML
     */
    private String extractTextFromLinks(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank())
            return htmlContent;

        // Tìm tất cả text content trong thẻ <a>
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<a[^>]*>([^<]+)</a>");
        java.util.regex.Matcher matcher = pattern.matcher(htmlContent);

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
     * Extract giá trị từ một selector cụ thể
     */
    private String extractValueFromSelector(Document doc, String query, String attribute, Boolean isList, String baseUrl) {
        if (isList != null && isList) {
            // Xử lý list elements
            Elements elements = doc.select(query);
            if (elements.isEmpty())
                return null;

            if (attribute != null && !attribute.isBlank()) {
                // Lấy attribute từ tất cả elements
                List<String> values = elements.stream()
                        .map(element -> element.attr(attribute))
                        .filter(val -> val != null && !val.isBlank())
                        .collect(Collectors.toList());
                return String.join(", ", values);
            } else {
                // Lấy text từ tất cả elements
                List<String> values = elements.stream()
                        .map(Element::text)
                        .filter(text -> text != null && !text.isBlank())
                        .collect(Collectors.toList());
                return String.join(", ", values);
            }
        } else {
            // Xử lý single element
            Element element = doc.selectFirst(query);
            if (element == null)
                return null;

            if (attribute != null && !attribute.isBlank()) {
                String value = element.attr(attribute);
                // Nếu là URL attribute, thêm domain nếu thiếu
                if (isUrlAttribute(attribute)) {
                    value = ensureFullUrl(value, baseUrl);
                }
                return value;
            } else {
                return element.text();
            }
        }
    }

    /**
     * Tìm hoặc tạo Movie
     */
    private Movie findOrCreateMovie(Map<String, String> extractedData, Document doc, String baseUrl, String titleQuery,
            String titleAttribute) {
        String thumbnailUrl = extractedData.get(SelectorMovieDetail.THUMBNAIL_URL.getValue());
        String title = extractedData.get(SelectorMovieDetail.TITLE.getValue());

        // Kiểm tra xem có phải phim bộ không (có bảng sequel)
        boolean isSeries = hasSequelTable(doc);

        if (isSeries) {
            // Nếu là phim bộ, lấy tên từ bảng sequel và tạo movie mới
            String seriesTitle = extractSeriesTitleFromSequelTable(doc, baseUrl, titleQuery, titleAttribute);
            if (seriesTitle != null && !seriesTitle.isBlank()) {
                title = seriesTitle;
                log.info("🎬 Phát hiện phim bộ, sử dụng tên từ bảng sequel: {}", seriesTitle);
            }

            // Kiểm tra tồn tại theo title series trước khi tạo
            if (title != null && !title.isBlank()) {
                Optional<Movie> existingByTitle = movieRepository.findByTitle(title);
                if (existingByTitle.isPresent()) {
                    log.info("🔄 Đã tồn tại movie SERIES theo title: {}", title);
                    return existingByTitle.get();
                }
            }

            // Tạo movie mới cho phim bộ
            Movie movie = Movie.builder()
                    .title(title != null ? title : "Unknown Series Title")
                    .slug(StringUtils.generateSlug(title))
                    .content(extractedData.get(SelectorMovieDetail.DESCRIPTION.getValue()))
                    .year(parseReleaseYear(extractedData.get(SelectorMovieDetail.RELEASE_YEAR.getValue())))
                    .thumbnailUrl(thumbnailUrl)
                    .posterUrl(extractedData.get(SelectorMovieDetail.POSTER_URL.getValue()))
                    .type(MovieType.SERIES)
                    .trailerUrl(extractedData.get(SelectorMovieDetail.TRAILER.getValue()))
                    .status(MovieStatus.ONGOING)
                    .build();

            movie = movieRepository.save(movie);
            log.info("🆕 Tạo movie mới cho phim bộ: {} với type: SERIES", movie.getTitle());
            return movie;
        } else {
            log.info("🎬 Phát hiện phim lẻ");

            // Upsert: nếu đã tồn tại theo title thì cập nhật, ngược lại tạo mới
            if (title != null && !title.isBlank()) {
                Optional<Movie> existingByTitle = movieRepository.findByTitle(title);
                if (existingByTitle.isPresent()) {
                    Movie movie = existingByTitle.get();
                    movie.setSlug(StringUtils.generateSlug(title));
                    movie.setContent(extractedData.get(SelectorMovieDetail.DESCRIPTION.getValue()));
                    movie.setYear(parseReleaseYear(extractedData.get(SelectorMovieDetail.RELEASE_YEAR.getValue())));
                    movie.setThumbnailUrl(thumbnailUrl);
                    movie.setPosterUrl(extractedData.get(SelectorMovieDetail.POSTER_URL.getValue()));
                    movie.setType(MovieType.SINGLE);
                    movie.setTrailerUrl(extractedData.get(SelectorMovieDetail.TRAILER.getValue()));
                    movie.setStatus(MovieStatus.COMPLETED);

                    movie = movieRepository.save(movie);
                    log.info("🔄 Cập nhật movie SINGLE theo title: {}", title);
                    return movie;
                }
            }

            Movie movie = Movie.builder()
                    .title(title != null ? title : "Unknown Title")
                    .slug(StringUtils.generateSlug(title))
                    .content(extractedData.get(SelectorMovieDetail.DESCRIPTION.getValue()))
                    .year(parseReleaseYear(extractedData.get(SelectorMovieDetail.RELEASE_YEAR.getValue())))
                    .thumbnailUrl(thumbnailUrl)
                    .posterUrl(extractedData.get(SelectorMovieDetail.POSTER_URL.getValue()))
                    .type(MovieType.SINGLE)
                    .trailerUrl(extractedData.get(SelectorMovieDetail.TRAILER.getValue()))
                    .status(MovieStatus.COMPLETED)
                    .build();

            movie = movieRepository.save(movie);
            log.info("🆕 Tạo movie mới cho phim lẻ: {} với type: SINGLE", movie.getTitle());
            return movie;
        }
    }

    /**
     * Kiểm tra xem có phải phim bộ không (có bảng sequel hoặc dropdown select)
     */
    private boolean hasSequelTable(Document doc) {
        // Trường hợp 1: Tìm bảng có id="Sequel" hoặc class chứa "sequel"
        Elements sequelTables = doc.select("table#Sequel, table[class*='sequel'], .htmlwrap table");

        if (!sequelTables.isEmpty()) {
            // Kiểm tra xem bảng có chứa dữ liệu sequel không
            for (Element table : sequelTables) {
                Elements rows = table.select("tbody tr");
                if (rows.size() > 1) { // Có ít nhất 2 dòng (header + 1 dòng dữ liệu)
                    log.info("✅ Phát hiện bảng sequel với {} dòng dữ liệu", rows.size() - 1);
                    return true;
                }
            }
        }

        // Trường hợp 2: Tìm dropdown select có name="Sequel_select" hoặc chứa nhiều tập
        Elements sequelSelects = doc.select("select[name*='Sequel'], select[name*='sequel'], .htmlwrap select");

        for (Element select : sequelSelects) {
            Elements options = select.select("option");
            if (options.size() > 1) { // Có ít nhất 2 options (không tính option mặc định)
                log.info("✅ Phát hiện dropdown sequel với {} tập", options.size());
                return true;
            }
        }

        log.info("ℹ️ Không phát hiện sequel (bảng hoặc dropdown), xử lý như phim lẻ");
        return false;
    }

    /**
     * Trích xuất tên phim bộ từ bảng sequel hoặc dropdown select
     */
    private String extractSeriesTitleFromSequelTable(Document doc, String baseUrl, String titleQuery,
            String titleAttribute) {
        // Trường hợp 1: Từ bảng sequel
        Elements sequelTables = doc.select("table#Sequel, table[class*='sequel'], .htmlwrap table");

        for (Element table : sequelTables) {
            Elements rows = table.select("tbody tr");
            if (rows.size() > 1) {
                Integer minEpisode = null;
                String minEpisodeHref = null;
                String titleWithoutNumber = null;

                for (int i = 0; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cells = row.select("td");
                    if (cells.size() >= 2) {
                        Element titleCell = cells.get(0);
                        Element episodeCell = cells.get(1);
                        Element link = titleCell.selectFirst("a");
                        if (link != null) {
                            String titleText = link.text().trim();
                            String episodeText = episodeCell.text().trim();
                            Integer ep = extractEpisodeNumberOrNull(episodeText);
                            if (ep != null) {
                                if (minEpisode == null || ep < minEpisode) {
                                    minEpisode = ep;
                                    minEpisodeHref = link.attr("href");
                                }
                            } else if (titleWithoutNumber == null && titleText != null && !titleText.isBlank()) {
                                titleWithoutNumber = titleText;
                            }
                        }
                    }
                }

                // Ưu tiên tên không có số
                if (titleWithoutNumber != null) {
                    String cleanTitle = titleWithoutNumber.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
                    log.info("✅ Sử dụng tên phim bộ không có số tập: {}", cleanTitle);
                    return cleanTitle;
                }

                // Nếu có link tập nhỏ nhất, truy cập và extract theo selector TITLE
                if (minEpisodeHref != null) {
                    String absoluteUrl = ensureFullUrl(minEpisodeHref, baseUrl);
                    String fetchedTitle = fetchTitleUsingSelector(absoluteUrl, titleQuery, titleAttribute, baseUrl);
                    if (fetchedTitle != null && !fetchedTitle.isBlank()) {
                        log.info("📺 Trích xuất tên phim bộ từ trang tập nhỏ nhất: {}", fetchedTitle);
                        return fetchedTitle.trim();
                    }
                }
            }
        }

        // Trường hợp 2: Từ dropdown select
        Elements sequelSelects = doc.select("select[name*='Sequel'], select[name*='sequel'], .htmlwrap select");

        for (Element select : sequelSelects) {
            Elements options = select.select("option");
            if (options.size() > 1) {
                Integer minEpisode = null;
                String minEpisodeHref = null;
                String titleWithoutNumber = null;

                for (int i = 0; i < options.size(); i++) {
                    Element option = options.get(i);
                    String titleText = option.text().trim();
                    Integer ep = extractEpisodeNumberOrNull(titleText);

                    if (ep != null) {
                        if (minEpisode == null || ep < minEpisode) {
                            minEpisode = ep;
                            // Thử lấy href từ attribute value hoặc data-href
                            String value = option.attr("value");
                            String dataHref = option.attr("data-href");
                            minEpisodeHref = (dataHref != null && !dataHref.isBlank()) ? dataHref : value;
                        }
                    } else if (titleWithoutNumber == null && titleText != null && !titleText.isBlank()) {
                        titleWithoutNumber = titleText;
                    }
                }

                if (titleWithoutNumber != null) {
                    String cleanTitle = titleWithoutNumber.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
                    log.info("✅ Sử dụng tên phim bộ không có số tập từ dropdown: {}", cleanTitle);
                    return cleanTitle;
                }

                if (minEpisodeHref != null) {
                    String absoluteUrl = ensureFullUrl(minEpisodeHref, baseUrl);
                    String fetchedTitle = fetchTitleUsingSelector(absoluteUrl, titleQuery, titleAttribute, baseUrl);
                    if (fetchedTitle != null && !fetchedTitle.isBlank()) {
                        log.info("📺 Trích xuất tên phim bộ từ dropdown tập nhỏ nhất: {}", fetchedTitle);
                        return fetchedTitle.trim();
                    }
                }
            }
        }

        log.warn("⚠️ Không thể trích xuất tên phim bộ từ sequel (bảng hoặc dropdown)");
        return null;
    }

    private String fetchTitleUsingSelector(String url, String titleQuery, String titleAttribute, String baseUrl) {
        try {
            String jsHtml = htmlFetcherHelper.fetchUrlWithJs(url);
            if (jsHtml == null || jsHtml.isBlank()) {
                return null;
            }
            Document doc = Jsoup.parse(jsHtml, url);
            if (titleQuery == null || titleQuery.isBlank()) {
                // Fallback: lấy title của trang nếu không có selector
                Element titleTag = doc.selectFirst("title");
                return titleTag != null ? titleTag.text() : null;
            }
            String value = extractValueFromSelector(doc, titleQuery, titleAttribute, false, baseUrl);
            return value;
        } catch (Exception e) {
            log.warn("⚠️ Lỗi khi fetch tiêu đề từ URL {}: {}", url, e.getMessage());
            return null;
        }
    }

    /**
     * Trích xuất tên phim bộ từ các dòng bảng đã sắp xếp theo số thứ tự tập
     */
    private String extractTitleFromSortedRows(Elements rows) {
        List<EpisodeInfo> episodeInfos = new ArrayList<>();
        String titleWithoutNumber = null;

        for (int i = 0; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");
            if (cells.size() >= 2) { // Cần ít nhất 2 cells: tên và số tập
                Element titleCell = cells.get(0);
                Element episodeCell = cells.get(1);

                Element link = titleCell.selectFirst("a");
                if (link != null) {
                    String title = link.text().trim();
                    String episodeText = episodeCell.text().trim();

                    if (title != null && !title.isBlank() && episodeText != null && !episodeText.isBlank()) {
                        Integer episodeNumber = extractEpisodeNumberOrNull(episodeText);

                        if (episodeNumber != null) {
                            // Có số tập, thêm vào danh sách để sắp xếp
                            episodeInfos.add(new EpisodeInfo(title, episodeNumber));
                        } else {
                            // Không có số tập, có thể đây là tên chung của phim bộ
                            if (titleWithoutNumber == null) {
                                titleWithoutNumber = title;
                                log.info("📺 Phát hiện tên phim bộ không có số tập: {}", title);
                            }
                        }
                    }
                }
            }
        }

        // Nếu có tên không có số, ưu tiên sử dụng
        if (titleWithoutNumber != null) {
            // Loại bỏ phần "ตอนที่ X" ở cuối (nếu có)
            String cleanTitle = titleWithoutNumber.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
            log.info("✅ Sử dụng tên phim bộ không có số tập: {}", cleanTitle);
            return cleanTitle;
        }

        // Nếu không có tên không có số, sắp xếp theo số tập
        if (!episodeInfos.isEmpty()) {
            // Sắp xếp theo số tập tăng dần
            episodeInfos.sort(Comparator.comparing(EpisodeInfo::getEpisodeNumber));

            // Lấy tập có số nhỏ nhất
            EpisodeInfo firstEpisode = episodeInfos.get(0);
            String title = firstEpisode.getTitle();

            // Loại bỏ phần "ตอนที่ X" ở cuối
            title = title.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
            log.info("✅ Sử dụng tên từ tập có số nhỏ nhất: {}", title);
            return title;
        }

        return null;
    }

    /**
     * Trích xuất tên phim bộ từ các options đã sắp xếp theo số thứ tự tập
     */
    private String extractTitleFromSortedOptions(Elements options) {
        List<EpisodeInfo> episodeInfos = new ArrayList<>();
        String titleWithoutNumber = null;

        for (int i = 0; i < options.size(); i++) {
            Element option = options.get(i);
            String title = option.text().trim();
            if (title != null && !title.isBlank()) {
                Integer episodeNumber = extractEpisodeNumberOrNull(title);

                if (episodeNumber != null) {
                    // Có số tập, thêm vào danh sách để sắp xếp
                    episodeInfos.add(new EpisodeInfo(title, episodeNumber));
                } else {
                    // Không có số tập, có thể đây là tên chung của phim bộ
                    if (titleWithoutNumber == null) {
                        titleWithoutNumber = title;
                        log.info("📺 Phát hiện tên phim bộ không có số tập từ dropdown: {}", title);
                    }
                }
            }
        }

        // Nếu có tên không có số, ưu tiên sử dụng
        if (titleWithoutNumber != null) {
            // Loại bỏ phần "ตอนที่ X" ở cuối (nếu có)
            String cleanTitle = titleWithoutNumber.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
            log.info("✅ Sử dụng tên phim bộ không có số tập từ dropdown: {}", cleanTitle);
            return cleanTitle;
        }

        // Nếu không có tên không có số, sắp xếp theo số tập
        if (!episodeInfos.isEmpty()) {
            // Sắp xếp theo số tập tăng dần
            episodeInfos.sort(Comparator.comparing(EpisodeInfo::getEpisodeNumber));

            // Lấy tập có số nhỏ nhất
            EpisodeInfo firstEpisode = episodeInfos.get(0);
            String title = firstEpisode.getTitle();

            // Loại bỏ phần "ตอนที่ X" ở cuối
            title = title.replaceAll("\\s*ตอนที่\\s*\\d+.*$", "").trim();
            log.info("✅ Sử dụng tên từ dropdown tập có số nhỏ nhất: {}", title);
            return title;
        }

        return null;
    }

    /**
     * Trích xuất số thứ tự tập từ text
     */
    private Integer extractEpisodeNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        // Tìm pattern "ตอนที่ X" hoặc "ep X" hoặc chỉ số (có thể có hoặc không có
        // khoảng cách)
        // Pattern \\s* sẽ match: "ep 1", "ep1", "episode 2", "episode2", "ตอนที่ 3",
        // "ตอนที่3"
        Pattern pattern = Pattern.compile("(?:ตอนที่|ep|episode|tập)\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("⚠️ Không thể parse số tập từ text: {}", text);
                return null;
            }
        }

        // Nếu không tìm thấy pattern, thử tìm số đơn lẻ
        pattern = Pattern.compile("\\b(\\d+)\\b");
        matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("⚠️ Không thể parse số từ text: {}", text);
                return null;
            }
        }

        return null;
    }

    /**
     * Trích xuất số thứ tự tập từ text, với fallback cho trường hợp không có số
     */
    private Integer extractEpisodeNumberWithFallback(String text, int fallbackNumber) {
        Integer episodeNumber = extractEpisodeNumber(text);
        if (episodeNumber != null) {
            return episodeNumber;
        }

        // Nếu không tìm thấy số, sử dụng fallback number
        log.info("ℹ️ Không tìm thấy số tập trong text: {}, sử dụng fallback: {}", text, fallbackNumber);
        return fallbackNumber;
    }

    /**
     * Trích xuất số thứ tự tập từ text, nếu không có số thì trả về null
     */
    private Integer extractEpisodeNumberOrNull(String text) {
        return extractEpisodeNumber(text);
    }

    /**
     * Lưu các entity liên quan
     */
    private void saveRelatedEntities(Movie movie, Map<String, String> extractedData, String baseUrl) {
        // Lưu Actors
        saveActors(movie, extractedData.get(SelectorMovieDetail.ACTORS.getValue()));

        // Lưu Categories
        saveCategories(movie, extractedData.get(SelectorMovieDetail.CATEGORY.getValue()));

        // Lưu Countries
        saveCountries(movie, extractedData.get(SelectorMovieDetail.COUNTRIES.getValue()));

        // Lưu Directors
        saveDirectors(movie, extractedData.get(SelectorMovieDetail.DIRECTORS.getValue()));

        // Lưu Episodes và ServerDatas

        saveEpisodesAndServerDatas(movie, extractedData, baseUrl);
    }

    /**
     * Lưu Actors
     */
    private void saveActors(Movie movie, String actorsData) {
        if (actorsData == null || actorsData.isBlank())
            return;

        Set<Actor> actors = new HashSet<>();
        String[] actorNames = actorsData.split(",");

        for (String actorName : actorNames) {
            String name = actorName.trim();
            // Kiểm tra kỹ hơn để tránh actor rỗng
            if (name != null && !name.isEmpty() && !name.isBlank()) {
                String slug = StringUtils.generateSlug(name);
                // Kiểm tra slug có hợp lệ không
                if (slug != null && !slug.isEmpty() && !slug.equals("unknown")) {
                    // Tìm actor đã tồn tại hoặc tạo mới
                    Actor actor = actorRepository.findByName(name)
                            .orElseGet(() -> {
                                Actor newActor = Actor.builder()
                                        .name(name)
                                        .slug(slug)
                                        .build();
                                log.debug("🆕 Tạo actor mới: {} -> {}", name, slug);
                                return newActor;
                            });

                    actors.add(actor);
                    log.debug("✅ Thêm actor: {} -> {}", name, actor.getSlug());
                } else {
                    log.warn("⚠️ Bỏ qua actor có slug không hợp lệ: {}", name);
                }
            } else {
                log.warn("⚠️ Bỏ qua actor rỗng: '{}'", actorName);
            }
        }

        if (!actors.isEmpty()) {
            // Chỉ save những actor mới (chưa có id)
            List<Actor> newActors = actors.stream()
                    .filter(actor -> actor.getId() == null)
                    .collect(Collectors.toList());

            if (!newActors.isEmpty()) {
                actorRepository.saveAll(newActors);
                log.info("✅ Lưu {} actors mới", newActors.size());
            }

            movie.setActors(actors);
            log.info("✅ Tổng cộng {} actors cho movie", actors.size());
        } else {
            log.warn("⚠️ Không có actor nào hợp lệ để lưu");
        }
    }

    /**
     * Lưu Categories
     */
    private void saveCategories(Movie movie, String categoryData) {
        if (categoryData == null || categoryData.isBlank())
            return;

        Set<Category> categories = new HashSet<>();
        String[] categoryNames = categoryData.split(",");

        for (String categoryName : categoryNames) {
            String name = categoryName.trim();
            // Kiểm tra kỹ hơn để tránh category rỗng
            if (name != null && !name.isEmpty() && !name.isBlank()) {
                String slug = StringUtils.generateSlug(name);
                // Kiểm tra slug có hợp lệ không
                if (slug != null && !slug.isEmpty() && !slug.equals("unknown")) {
                    // Tìm category đã tồn tại hoặc tạo mới
                    Category category = categoryRepository.findByName(name)
                            .orElseGet(() -> {
                                Category newCategory = Category.builder()
                                        .name(name)
                                        .slug(slug)
                                        .build();
                                log.debug("🆕 Tạo category mới: {} -> {}", name, slug);
                                return newCategory;
                            });

                    categories.add(category);
                    log.debug("✅ Thêm category: {} -> {}", name, category.getSlug());
                } else {
                    log.warn("⚠️ Bỏ qua category có slug không hợp lệ: {}", name);
                }
            } else {
                log.warn("⚠️ Bỏ qua category rỗng: '{}'", categoryName);
            }
        }

        if (!categories.isEmpty()) {
            // Chỉ save những category mới (chưa có id)
            List<Category> newCategories = categories.stream()
                    .filter(cat -> cat.getId() == null)
                    .collect(Collectors.toList());

            if (!newCategories.isEmpty()) {
                categoryRepository.saveAll(newCategories);
                log.info("✅ Lưu {} categories mới", newCategories.size());
            }

            movie.setCategories(categories);
            log.info("✅ Tổng cộng {} categories cho movie", categories.size());
        } else {
            log.warn("⚠️ Không có category nào hợp lệ để lưu");
        }
    }

    /**
     * Lưu Countries
     */
    private void saveCountries(Movie movie, String countriesData) {
        if (countriesData == null || countriesData.isBlank())
            return;

        Set<Country> countries = new HashSet<>();
        String[] countryNames = countriesData.split(",");

        for (String countryName : countryNames) {
            String name = countryName.trim();
            // Kiểm tra kỹ hơn để tránh country rỗng
            if (name != null && !name.isEmpty() && !name.isBlank()) {
                String slug = StringUtils.generateSlug(name);
                // Kiểm tra slug có hợp lệ không
                if (slug != null && !slug.isEmpty() && !slug.equals("unknown")) {
                    // Tìm country đã tồn tại hoặc tạo mới
                    Country country = countryRepository.findByName(name)
                            .orElseGet(() -> {
                                Country newCountry = Country.builder()
                                        .name(name)
                                        .slug(slug)
                                        .build();
                                log.debug("🆕 Tạo country mới: {} -> {}", name, slug);
                                return newCountry;
                            });

                    countries.add(country);
                    log.debug("✅ Thêm country: {} -> {}", name, country.getSlug());
                } else {
                    log.warn("⚠️ Bỏ qua country có slug không hợp lệ: {}", name);
                }
            } else {
                log.warn("⚠️ Bỏ qua country rỗng: '{}'", countryName);
            }
        }

        if (!countries.isEmpty()) {
            // Chỉ save những country mới (chưa có id)
            List<Country> newCountries = countries.stream()
                    .filter(country -> country.getId() == null)
                    .collect(Collectors.toList());

            if (!newCountries.isEmpty()) {
                countryRepository.saveAll(newCountries);
                log.info("✅ Lưu {} countries mới", newCountries.size());
            }

            movie.setCountries(countries);
            log.info("✅ Tổng cộng {} countries cho movie", countries.size());
        } else {
            log.warn("⚠️ Không có country nào hợp lệ để lưu");
        }
    }

    /**
     * Lưu Directors
     */
    private void saveDirectors(Movie movie, String directorsData) {
        if (directorsData == null || directorsData.isBlank())
            return;

        Set<Director> directors = new HashSet<>();
        String[] directorNames = directorsData.split(",");

        for (String directorName : directorNames) {
            String name = directorName.trim();
            // Kiểm tra kỹ hơn để tránh director rỗng
            if (name != null && !name.isEmpty() && !name.isBlank()) {
                String slug = StringUtils.generateSlug(name);
                // Kiểm tra slug có hợp lệ không
                if (slug != null && !slug.isEmpty() && !slug.equals("unknown")) {
                    // Tìm director đã tồn tại hoặc tạo mới
                    Director director = directorRepository.findByName(name)
                            .orElseGet(() -> {
                                Director newDirector = Director.builder()
                                        .name(name)
                                        .slug(slug)
                                        .build();
                                log.debug("🆕 Tạo director mới: {} -> {}", name, slug);
                                return newDirector;
                            });

                    directors.add(director);
                    log.debug("✅ Thêm director: {} -> {}", name, director.getSlug());
                } else {
                    log.warn("⚠️ Bỏ qua director có slug không hợp lệ: {}", name);
                }
            } else {
                log.warn("⚠️ Bỏ qua director rỗng: '{}'", directorName);
            }
        }

        if (!directors.isEmpty()) {
            // Chỉ save những director mới (chưa có id)
            List<Director> newDirectors = directors.stream()
                    .filter(director -> director.getId() == null)
                    .collect(Collectors.toList());

            if (!newDirectors.isEmpty()) {
                directorRepository.saveAll(newDirectors);
                log.info("✅ Lưu {} directors mới", newDirectors.size());
            }

            movie.setDirectors(directors);
            log.info("✅ Tổng cộng {} directors cho movie", directors.size());
        } else {
            log.warn("⚠️ Không có director nào hợp lệ để lưu");
        }
    }

    /**
     * Lưu Episodes và ServerDatas
     * todo:
     * kiểm tra có extract data từ SelectorMovieDetail.SUBTITLE_BUTTON và SelectorMovieDetail.BUDDING_BUTTON không
     * nếu CÓ thì selenium click vô từng cái và cào serverName bằng extract data từ SelectorMovieDetail.EPISODE_SERVER_NAME
     * tạo server data tương ứng với episode đó với serverName là episodeServerName extracted data từ SelectorMovieDetail.EPISODE_SERVER_NAME
     * nếu KHÔNG CÓ thì chỉ cần tạo episode với serverName là episodeServerName extracted data từ SelectorMovieDetail.EPISODE_SERVER_NAME và episode từ extractedData.get(SelectorMovieDetail.VIDEO_URL.getValue());
     *
     * mẫu trich xuất kiểm tra có subtitle và budding không
     * <tr>
     *
     * 	<th class="selectmvbutton lmselect-1" style="width:24.5%; "><span class="halim-btn halim-btn-2 halim-info-1-1 box-shadow2" data-post-id="169359" data-server="1" data-episode="0" data-position="last" data-embed="1" data-type="none" data-title="My Girlfriend is the Man (2025) เมื่อแฟนผมกลายเป็นหนุ่มสุดฮอต style=" cursor:="" pointer;"=""><i class="hl-server2"></i> พากย์ไทย</span></th>
     * <th class="selectmvbutton lmselect-2" style="width: 24.5%;">
     * 			<span class="halim-btn halim-btn-2halim-info-2-1 box-shadow2 active" data-post-id="169359" data-server="2" data-episode="0" data-position="" data-embed="0" data-type="none" data-title="My Girlfriend is the Man (2025) เมื่อแฟนผมกลายเป็นหนุ่มสุดฮอต style=" cursor:="" pointer;"=""><i class="hl-server2"></i> ซับไทย</span>
     * 			</th>
     * <th class="selectmvbutton" style="width: 24.5%; "><a href=""><span class="halim-btn halim-btn-2 halim-info-1-1 box-shadow2" style="cursor: pointer; color: #ffffff;"><i class="font dir="auto" style="vertical-align: inherit;"><font dir="auto" style="vertical-align: inherit;">&lt; ก่อนหน้า</font></font></span></a></th>
     * 				<th class="selectmvbutton" style="width: 24.5%; "><a href="https://www.123hdtv.com/my-girlfriend-is-the-man-ep-2"><span class="halim-btn halim-btn-2 halim-info-1-1 box-shadow2" style="cursor: pointer; color: #ffffff;"><i class="font dir="auto" style="vertical-align: inherit;"><font dir="auto" style="vertical-align: inherit;">ถัดไป &gt;</font></font></span></a></th>
     * 				</tr>
     *
     * 			nó là các th với 	selectmvbutton lmselect-1 và selectmvbutton lmselect-2 
     */
    private void saveEpisodesAndServerDatas(Movie movie, Map<String, String> extractedData, String baseUrl) {
        String videoUrl = extractedData.get(SelectorMovieDetail.VIDEO_URL.getValue());

        String videoEmbedLink;
        if (videoUrl != null && !videoUrl.isBlank()) {

            // Kiểm tra có SUBTITLE_BUTTON và BUDDING_BUTTON không
            String subtitleButtonSelector = extractedData.get(SelectorMovieDetail.SUBTITLE_BUTTON.getValue());
            String buddingButtonSelector = extractedData.get(SelectorMovieDetail.BUDDING_BUTTON.getValue());

            if (subtitleButtonSelector != null && !subtitleButtonSelector.isBlank() || 
                buddingButtonSelector != null && !buddingButtonSelector.isBlank()) {
                
                log.info("Tìm thấy subtitle/budding buttons, xử lý bằng Selenium...");
                handleSubtitleAndBuddhaButtons(movie, extractedData, baseUrl);
                
            } else {
                log.info("Không có subtitle/budding buttons, tạo episode cơ bản...");
                createBasicEpisode(movie, extractedData, videoUrl);
            }
        }
    }

    /**
     * Xử lý subtitle và buddha buttons bằng Selenium
     */
    private void handleSubtitleAndBuddhaButtons(Movie movie, Map<String, String> extractedData, String baseUrl) {
        WebDriver driver = null;
        try {
            // Tạo WebDriver tương tự VideoLinkExtractor
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // Chạy không giao diện
            options.addArguments("--mute-audio"); // Tắt âm thanh
            options.addArguments("--disable-audio"); // Vô hiệu hóa audio
            options.addArguments("--disable-images"); // Không load hình ảnh để nhanh hơn
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            options.addArguments("--disable-web-security");
            options.addArguments("--allow-running-insecure-content");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");

            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

            // Loại bỏ thuộc tính webdriver
            ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");

            log.info("Đang truy cập: {} để xử lý subtitle/budding buttons", baseUrl);
            driver.get(baseUrl);
            Thread.sleep(3000);

            // Xử lý subtitle button
            String subtitleButtonSelector = extractedData.get(SelectorMovieDetail.SUBTITLE_BUTTON.getValue());
            if (subtitleButtonSelector != null && !subtitleButtonSelector.isBlank()) {
                handleButtonClick(driver, wait, movie, extractedData, "subtitle", subtitleButtonSelector, baseUrl);
            }

            // Xử lý budding button  
            String buddingButtonSelector = extractedData.get(SelectorMovieDetail.BUDDING_BUTTON.getValue());
            if (buddingButtonSelector != null && !buddingButtonSelector.isBlank()) {
                handleButtonClick(driver, wait, movie, extractedData, "budding", buddingButtonSelector, baseUrl);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý subtitle/budding buttons: {}", e.getMessage());
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("Đã đóng WebDriver");
            }
        }
    }

    /**
     * Xử lý click button và tạo episode/server data
     */
    private void handleButtonClick(WebDriver driver, WebDriverWait wait, Movie movie, Map<String, String> extractedData, String serverType, String buttonSelector, String baseUrl) {
        try {
            log.info("Đang xử lý {} button với selector: {}", serverType, buttonSelector);
            
            // Tìm và click vào span bên trong th element
            WebElement thElement = null;
            WebElement spanElement = null;
            
            try {
                // Tìm th element trước
                thElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("budding".equals(serverType)
                        ? "#content > div > table > tbody > tr > th.selectmvbutton.lmselect-1"
                        : "#content > div > table > tbody > tr > th.selectmvbutton.lmselect-2")));
                
                // Tìm span bên trong th element
                spanElement = thElement.findElement(By.cssSelector("span.halim-btn"));
                log.info("Tìm thấy span element bên trong th");
                
            } catch (Exception e) {
                log.warn("Không tìm thấy span trong th, thử tìm trực tiếp...");
                try {
                    // Fallback: tìm span trực tiếp
                    spanElement = wait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector(buttonSelector + " span.halim-btn")
                    ));
                    log.info("Tìm thấy span element trực tiếp");
                } catch (Exception e2) {
                    log.error("Không thể tìm thấy span element cho {}: {}", serverType, e2.getMessage());
                    return;
                }
            }
            
            // Click vào span element
            try {
                log.info("Đang click vào span element cho {}...", serverType);
                spanElement.click();
                log.info("Click thành công bằng click() method");
            } catch (Exception e) {
                log.warn("Click thường thất bại, thử bằng JavaScript...");
                try {
                    // Click bằng JavaScript
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", spanElement);
                    log.info("Click thành công bằng JavaScript");
                } catch (Exception e2) {
                    log.warn("Click JavaScript thất bại, thử scroll và click...");
                    try {
                        // Scroll đến span và click
                        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", spanElement);
                        Thread.sleep(1000);
                        spanElement.click();
                        log.info("Click thành công sau khi scroll");
                    } catch (Exception e3) {
                        log.error("Tất cả cách click đều thất bại cho {}: {}", serverType, e3.getMessage());
                        return;
                    }
                }
            }
            
            // Đợi page load sau khi click
            log.info("Đang đợi page load sau khi click...");
            Thread.sleep(1000);

            // Extract server name mới từ page sau khi click todo: xoa ham extract server name for page => su dung extractedData.get(SelectorMovieDetail.EPISODE_SERVER_NAME.getValue());
            String newServerName = extractedData.get(SelectorMovieDetail.EPISODE_SERVER_NAME.getValue());
            
            if (newServerName != null && !newServerName.isBlank()) {
                log.info("Tìm thấy server name mới cho {}: {}", serverType, newServerName);
                
                // Tạo episode mới với server name này
                Episode newEpisode = episodeRepository.findByMovieIdAndServerName(movie.getId(), newServerName)
                        .orElse(null);
                if (newEpisode == null) {
                    newEpisode = Episode.builder()
                            .serverName(newServerName)
                            .movie(movie)
                            .build();
                    newEpisode = episodeRepository.save(newEpisode);
                    log.info("✅ Đã tạo episode mới cho {}: {}", serverType, newServerName);
                }

                // Tạo server data tương ứng - lấy videoUrl mới từ page sau khi click
                String newVideoUrl = extractVideoUrlFromPageAfterClick(driver, baseUrl);
                if (newVideoUrl != null && !newVideoUrl.isBlank()) {
                    createServerDataForEpisode(newEpisode, movie, extractedData, newVideoUrl, serverType);
                } else {
                    log.warn("Không tìm thấy videoUrl mới sau khi click {} button", serverType);
                }
                
            } else {
                log.warn("Không tìm thấy server name mới cho {}", serverType);
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý {} button: {}", serverType, e.getMessage());
        }
    }

    /**
     * Tạo episode cơ bản khi không có subtitle/budding buttons
     */
    private void createBasicEpisode(Movie movie, Map<String, String> extractedData, String videoUrl) {
        String episodeServerName = extractedData.get(SelectorMovieDetail.EPISODE_SERVER_NAME.getValue());
        
        // Đảm bảo mỗi episode chỉ có 1 bộ server data riêng
        Episode episode = episodeRepository.findByMovieIdAndServerName(movie.getId(), episodeServerName)
                .orElse(null);
        if (episode == null) {
            episode = Episode.builder()
                    .serverName(episodeServerName)
                    .movie(movie)
                    .build();
            episode = episodeRepository.save(episode);
        }

        // Tạo server data cơ bản
        createServerDataForEpisode(episode, movie, extractedData, videoUrl, "basic");
    }

    /**
     * Tạo server data cho episode
     */
    private void createServerDataForEpisode(Episode episode, Movie movie, Map<String, String> extractedData, String videoUrl, String serverType) {
        String videoEmbedLink;
        String chosenDownloadUrl;
        
        if (videoUrl.contains("player.stream1689")) { // nung2-hdd.com
            videoEmbedLink = videoLinkExtractor.extractVideoLink(videoUrl);
            chosenDownloadUrl = videoEmbedLink;
        } else { // video contains "main.24playerhd.com"
            videoEmbedLink = videoUrl;
            chosenDownloadUrl = videoUrl;
        }

        // Xác định tiêu đề của server data
        String sdTitle = extractedData.get(SelectorMovieDetail.TITLE.getValue());
        if (sdTitle == null || sdTitle.isBlank()) {
            sdTitle = movie.getTitle();
        }
        
        // Tạo slug dựa trên server type
        String sdSlug = StringUtils.generateSlug(sdTitle) + "-" + serverType;

        // Kiểm tra server data đã tồn tại chưa
        ServerData serverData = null;
        if (episode.getServerData() != null) {
            for (ServerData sd : episode.getServerData()) {
                if (sdSlug.equals(sd.getSlug())) {
                    serverData = sd;
                    break;
                }
            }
        }
        
        if (serverData == null) {
            serverData = ServerData.builder()
                    .episode(episode)
                    .slug(sdSlug)
                    .build();
        }

        serverData.setName(sdTitle + " (" + serverType + ")");
        serverData.setFilename(sdTitle + " (" + serverType + ")");
        serverData.setLink_embed(videoEmbedLink);

        // Lưu trước để có serverDataId, sau đó tải M3U8
        serverData = serverDataRepository.save(serverData);
        Boolean isSingle = MovieType.SINGLE.equals(movie.getType());
        m3U8DownloadService.downloadM3U8Video(chosenDownloadUrl, movie.getId(), serverData.getId(), isSingle);
        String localMasterPath = m3U8DownloadService
                .buildMasterLocalPath(movie.getId(), serverData.getId(), isSingle)
                .replace("../../../", "");

        // Lưu link m3u8 cho server data
        String playlistBaseUrl = m3u8Properties.getPlaylistBaseUrl();
        String normalizedBase = playlistBaseUrl.endsWith("/")
                ? playlistBaseUrl.substring(0, playlistBaseUrl.length() - 1)
                : playlistBaseUrl;

        String linkToStore;
        if (localMasterPath.startsWith("data/playlist/")) {
            String relative = localMasterPath.substring("data/playlist".length());
            linkToStore = normalizedBase + relative;
        } else {
            linkToStore = normalizedBase;
        }

        serverData.setLink_m3u8(linkToStore);
        serverDataRepository.save(serverData);
        log.info("✅ Lưu/cập nhật Server Data cho episode {} (m3u8 theo movieId/serverDataId)", serverType);
    }

    /**
     * Extract server name từ page sau khi click button
     */
    private String extractServerNameFromPage(WebDriver driver, Map<String, String> extractedData) {
        try {
            // Thử extract từ EPISODE_SERVER_NAME selector
            String serverNameSelector = extractedData.get(SelectorMovieDetail.EPISODE_SERVER_NAME.getValue());
            if (serverNameSelector != null && !serverNameSelector.isBlank()) {
                try {
                    WebElement serverNameElement = driver.findElement(By.cssSelector(serverNameSelector));
                    String serverName = serverNameElement.getText();
                    if (serverName != null && !serverName.isBlank()) {
                        return serverName.trim();
                    }
                } catch (Exception e) {
                    log.debug("Không thể extract server name từ selector: {}", e.getMessage());
                }
            }
            
            // Fallback: tìm các element có thể chứa server name
            String[] fallbackSelectors = {
                "[class*='server']", "[class*='episode']", "[class*='player']",
                ".server-name", ".episode-name", ".player-name"
            };
            
            for (String selector : fallbackSelectors) {
                try {
                    List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                    for (WebElement element : elements) {
                        String text = element.getText();
                        if (text != null && !text.isBlank() && text.length() < 100) {
                            return text.trim();
                        }
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
        } catch (Exception e) {
            log.error("Lỗi khi extract server name từ page: {}", e.getMessage());
        }
        
        return null;
    }

    /**
     * Extract video URL mới từ page sau khi click button, sử dụng method extractValueFromSelector có sẵn
     */
    private String extractVideoUrlFromPageAfterClick(WebDriver driver, String baseUrl) {
        try {
            // Lấy page source từ WebDriver
            String pageSource = driver.getPageSource();
            
            // Convert thành Jsoup Document để sử dụng extractValueFromSelector
            Document doc = Jsoup.parse(pageSource, baseUrl);
            
            // todo: khong hardcode
            String videoUrlSelector = ".embed-responsive-item";
            if (videoUrlSelector != null && !videoUrlSelector.isBlank()) {
                String newVideoUrl = extractValueFromSelector(doc, videoUrlSelector, "src", false, baseUrl);
                
                if (newVideoUrl != null && !newVideoUrl.isBlank()) {
                    log.info("Tìm thấy video URL mới: {}", newVideoUrl);
                    return newVideoUrl;
                }
            }
            
            log.warn("Không tìm thấy video URL mới sau khi click button");
            return null;
            
        } catch (Exception e) {
            log.error("Lỗi khi extract video URL từ page: {}", e.getMessage());
            return null;
        }
    }



    // legacy helper removed; use m3U8DownloadService directly and build path via
    // buildMasterLocalPath

    private Integer parseReleaseYear(String yearStr) {
        if (yearStr == null || yearStr.isBlank())
            return null;

        try {
            return Integer.parseInt(yearStr.trim());
        } catch (NumberFormatException e) {
            log.warn("⚠️ Không thể parse release year: {}", yearStr);
            return null;
        }
    }

    private boolean isUrlAttribute(String attribute) {
        if (attribute == null)
            return false;

        String lowerAttr = attribute.toLowerCase();
        return lowerAttr.equals("href") ||
                lowerAttr.equals("src") ||
                lowerAttr.equals("data-src") ||
                lowerAttr.equals("data-original") ||
                lowerAttr.equals("data-lazy-src") ||
                lowerAttr.equals("data-url") ||
                lowerAttr.equals("url") ||
                lowerAttr.equals("link") ||
                lowerAttr.startsWith("data-") && lowerAttr.contains("url");
    }

    private String ensureFullUrl(String url, String baseUrl) {
        if (url == null || url.isBlank()) {
            return url;
        }

        // Nếu URL đã có protocol (http/https), trả về nguyên bản
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }

        try {
            // Lấy domain từ baseUrl
            java.net.URI baseUri = new java.net.URI(baseUrl);
            String domain = baseUri.getScheme() + "://" + baseUri.getHost();

            // Nếu URL bắt đầu bằng /, thêm domain vào trước
            if (url.startsWith("/")) {
                return domain + url;
            }

            // Nếu URL không bắt đầu bằng /, thêm domain và / vào trước
            return domain + "/" + url;
        } catch (java.net.URISyntaxException e) {
            log.warn("Không thể parse baseUrl: {}", baseUrl);
            return url;
        }
    }
}
