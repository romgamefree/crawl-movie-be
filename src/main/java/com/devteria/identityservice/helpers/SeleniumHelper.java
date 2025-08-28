package com.devteria.identityservice.helpers;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
public class SeleniumHelper {

    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS = 60;

    static {
        // Setup WebDriverManager
        WebDriverManager.chromedriver().setup();
    }

    /**
     * Tạo WebDriver với Chrome headless
     */
    public WebDriver createChromeDriver() {
        ChromeOptions options = new ChromeOptions();

        // Headless mode
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");

        // User agent
        options.addArguments(
                "--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");

        // Disable images và CSS để tăng tốc
        options.addArguments("--disable-images");
        options.addArguments("--disable-css");

        WebDriver driver = new ChromeDriver(options);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(DEFAULT_PAGE_LOAD_TIMEOUT_SECONDS));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        return driver;
    }

    /**
     * Fetch HTML với JavaScript rendering
     */
    public String fetchHtmlWithJs(String url) {
        WebDriver driver = null;
        try {
            log.info("🔄 Bắt đầu fetch HTML với JavaScript từ: {}", url);

            driver = createChromeDriver();
            driver.get(url);

            // Chờ page load xong
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));

            // Chờ cho đến khi document ready
            wait.until(webDriver -> {
                String readyState = (String) ((JavascriptExecutor) webDriver)
                        .executeScript("return document.readyState");
                return "complete".equals(readyState);
            });

            // Chờ thêm 2 giây để JavaScript load hoàn toàn
            Thread.sleep(2000);

            // Lấy HTML sau khi JavaScript đã render
            String html = driver.getPageSource();

            log.info("✅ Đã fetch HTML thành công với JavaScript, length: {}", html.length());
            return html;

        } catch (Exception e) {
            log.error("❌ Lỗi khi fetch HTML với JavaScript từ {}: {}", url, e.getMessage(), e);
            return null;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("⚠️ Lỗi khi đóng WebDriver: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Fetch HTML với JavaScript và chờ element cụ thể xuất hiện
     */
    public String fetchHtmlWithJsAndWaitForElement(String url, String elementSelector) {
        WebDriver driver = null;
        try {
            log.info("🔄 Bắt đầu fetch HTML với JavaScript và chờ element: {} từ: {}", elementSelector, url);

            driver = createChromeDriver();
            driver.get(url);

            // Chờ element xuất hiện
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
            wait.until(webDriver -> webDriver.findElement(By.cssSelector(elementSelector)) != null);

            // Chờ thêm 1 giây để element load hoàn toàn
            Thread.sleep(1000);

            String html = driver.getPageSource();
            log.info("✅ Đã fetch HTML thành công và tìm thấy element: {}", elementSelector);
            return html;

        } catch (Exception e) {
            log.error("❌ Lỗi khi fetch HTML với JavaScript và chờ element {} từ {}: {}",
                    elementSelector, url, e.getMessage(), e);
            return null;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    log.warn("⚠️ Lỗi khi đóng WebDriver: {}", e.getMessage());
                }
            }
        }
    }
}
