package com.devteria.identityservice.service;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


@Service
@Slf4j
public class VideoLinkExtractor implements AutoCloseable {
    private WebDriver driver;
    private WebDriverWait wait;

    public VideoLinkExtractor() {
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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // Loại bỏ thuộc tính webdriver
        ((JavascriptExecutor) driver).executeScript("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})");
    }

    public String extractVideoLink(String url) {
        try {
            log.info("Đang truy cập: " + url);
            driver.get(url);
            Thread.sleep(3000);

            // Click vào video để bắt đầu
            clickVideo();

            // Đợi 3 giây
            log.info("Đang đợi 3 giây...");
            Thread.sleep(3000);

            // Skip quảng cáo
            skipAd();

            // Đợi video load và lấy link thực tế
            Thread.sleep(5000);

            return getRealVideoLink();

        } catch (Exception e) {
            log.info("Lỗi: " + e.getMessage());
            return null;
        }
    }

    private void clickVideo() {
        try {
            // Thử các cách khác nhau để click vào video
            String[] videoSelectors = {
                    ".jw-video",
                    ".jwplayer",
                    "[id*='jwplayer']",
                    ".jw-media",
                    "video",
                    ".media-player",
                    ".video-player"
            };

            WebElement videoPlayer = null;
            for (String selector : videoSelectors) {
                try {
                    videoPlayer = driver.findElement(By.cssSelector(selector));
                    break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (videoPlayer != null) {
                log.info("Đang click vào video...");
                videoPlayer.click();
            } else {
                // Click vào giữa màn hình
                ((JavascriptExecutor) driver).executeScript(
                        "document.elementFromPoint(window.innerWidth/2, window.innerHeight/2).click();"
                );
            }

        } catch (Exception e) {
            log.info("Lỗi khi click video: " + e.getMessage());
        }
    }

    private void skipAd() {
        try {
            log.info("Đang tìm nút skip quảng cáo...");

            String[] skipSelectors = {
                    "button.btn.btn-skip",
                    ".btn-skip",
                    "[class*='skip']",
                    "button[class*='skip']",
                    ".ad-skip",
                    ".skip-ad",
                    ".jw-skip",
                    ".jwplayer .jw-skip"
            };

            WebElement skipButton = null;
            for (String selector : skipSelectors) {
                try {
                    skipButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(selector)));
                    log.info("Tìm thấy nút skip: " + selector);
                    break;
                } catch (Exception e) {
                    continue;
                }
            }

            if (skipButton != null) {
                skipButton.click();
                log.info("Đã skip quảng cáo!");
            } else {
                // Thử bằng JavaScript
                ((JavascriptExecutor) driver).executeScript(
                        "var skipBtn = document.querySelector('button[class*=\"skip\"], .btn-skip, .ad-skip'); " +
                                "if(skipBtn && skipBtn.offsetParent !== null) skipBtn.click();"
                );
                log.info("Thử skip bằng JavaScript");
            }

        } catch (Exception e) {
            log.info("Lỗi khi skip ad: " + e.getMessage());
        }
    }

    private String getRealVideoLink() {
        try {
            log.info("Đang tìm link video thực tế...");

            // Cách 1: Tìm iframe và lấy src
            String iframeSrc = getIframeSrc();
            if (iframeSrc != null && !iframeSrc.isEmpty()) {
                log.info("Tìm thấy iframe src: " + iframeSrc);
                return iframeSrc;
            }

            // Cách 2: Tìm video element và lấy src
            String videoSrc = getVideoSrc();
            if (videoSrc != null && !videoSrc.isEmpty()) {
                log.info("Tìm thấy video src: " + videoSrc);
                return videoSrc;
            }

            // Cách 3: Tìm trong JavaScript variables
            String jsVideoUrl = getVideoUrlFromJS();
            if (jsVideoUrl != null && !jsVideoUrl.isEmpty()) {
                log.info("Tìm thấy video URL từ JavaScript: " + jsVideoUrl);
                return jsVideoUrl;
            }

            // Cách 4: Tìm trong network requests
            String networkUrl = getVideoUrlFromNetwork();
            if (networkUrl != null && !networkUrl.isEmpty()) {
                log.info("Tìm thấy video URL từ network: " + networkUrl);
                return networkUrl;
            }

            log.info("Không tìm thấy link video thực tế");
            return null;

        } catch (Exception e) {
            log.info("Lỗi khi lấy video link: " + e.getMessage());
            return null;
        }
    }

    private String getIframeSrc() {
        try {
            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            for (WebElement iframe : iframes) {
                String src = iframe.getAttribute("src");
                if (src != null && (src.contains(".mp4") || src.contains("video") ||
                        src.contains("stream") || src.contains("player"))) {
                    return src;
                }
            }
        } catch (Exception e) {
            log.info("Lỗi khi tìm iframe: " + e.getMessage());
        }
        return null;
    }

    private String getVideoSrc() {
        try {
            List<WebElement> videos = driver.findElements(By.tagName("video"));
            for (WebElement video : videos) {
                String src = video.getAttribute("src");
                if (src != null && !src.isEmpty()) {
                    return src;
                }

                // Tìm trong source elements
                List<WebElement> sources = video.findElements(By.tagName("source"));
                for (WebElement source : sources) {
                    String sourceSrc = source.getAttribute("src");
                    if (sourceSrc != null && !sourceSrc.isEmpty()) {
                        return sourceSrc;
                    }
                }
            }
        } catch (Exception e) {
            log.info("Lỗi khi tìm video src: " + e.getMessage());
        }
        return null;
    }

    private String getVideoUrlFromJS() {
        try {
            // Thử lấy từ các biến JavaScript phổ biến
            String[] jsQueries = {
                    "return window.jwplayer && window.jwplayer().getPlaylist ? window.jwplayer().getPlaylist()[0].file : null;",
                    "return window.videoUrl || window.streamUrl || window.playerUrl;",
                    "return document.querySelector('video') ? document.querySelector('video').src : null;",
                    "return window.player && window.player.src ? window.player.src : null;"
            };

            for (String query : jsQueries) {
                try {
                    Object result = ((JavascriptExecutor) driver).executeScript(query);
                    if (result != null && !result.toString().isEmpty()) {
                        return result.toString();
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            log.info("Lỗi khi tìm video URL từ JS: " + e.getMessage());
        }
        return null;
    }

    private String getVideoUrlFromNetwork() {
        try {
            // Tìm các request có chứa video format
            String script =
                    "var urls = [];" +
                            "var originalFetch = window.fetch;" +
                            "window.fetch = function() {" +
                            "  var url = arguments[0];" +
                            "  if (typeof url === 'string' && (url.includes('.mp4') || url.includes('.m3u8') || url.includes('video'))) {" +
                            "    urls.push(url);" +
                            "  }" +
                            "  return originalFetch.apply(this, arguments);" +
                            "};" +
                            "return urls;";

            Object result = ((JavascriptExecutor) driver).executeScript(script);
            if (result instanceof List) {
                List<?> urls = (List<?>) result;
                if (!urls.isEmpty()) {
                    return urls.get(urls.size() - 1).toString();
                }
            }
        } catch (Exception e) {
            log.info("Lỗi khi tìm video URL từ network: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.quit();
            log.info("Đã đóng driver");
        }
    }
}