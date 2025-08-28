package com.devteria.identityservice.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {

    public static void createFolder(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                System.out.println("📂 Tạo thư mục: " + folderPath);
            }
        }
    }

    public static void saveFileContent(String filePath, String content) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + filePath, e);
        }
    }

    public static void saveFileBytes(String filePath, byte[] data) {
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // đảm bảo có thư mục cha
            Files.write(file.toPath(), data);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file: " + filePath, e);
        }
    }

    public static String extractCleanFileName(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        int queryIndex = fileName.indexOf("?");
        if (queryIndex != -1) {
            fileName = fileName.substring(0, queryIndex);
        }
        return fileName;
    }
}

