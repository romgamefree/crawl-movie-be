//package com.devteria.identityservice.controller;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import jakarta.servlet.http.HttpServletRequest;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@RestController
//@RequestMapping("/local_copy")
//@RequiredArgsConstructor
//@Slf4j
//public class LocalCopyController {
//
//    private static final String LOCAL_COPY_PATH = "local_copy";
//
//    /**
//     * Serve files từ thư mục local_copy
//     */
//    @GetMapping("/**")
//    public ResponseEntity<String> serveLocalCopyFile(HttpServletRequest request) {
//        try {
//            String requestPath = getRequestPath(request);
//            log.info("📁 Request local_copy file: {}", requestPath);
//
//            Path filePath = Paths.get(LOCAL_COPY_PATH, requestPath);
//
//            if (!Files.exists(filePath)) {
//                log.warn("⚠️ File không tồn tại: {}", filePath);
//                return ResponseEntity.notFound().build();
//            }
//
//            // Đọc nội dung file
//            String content = Files.readString(filePath);
//
//            // Set headers
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.TEXT_PLAIN);
//            headers.set("Access-Control-Allow-Origin", "*");
//            headers.set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
//            headers.set("Access-Control-Allow-Headers", "*");
//
//            log.info("✅ Served local_copy file: {} ({} bytes)", requestPath, content.length());
//            return ResponseEntity.ok().headers(headers).body(content);
//
//        } catch (IOException e) {
//            log.error("❌ Lỗi khi đọc file: {}", e.getMessage(), e);
//            return ResponseEntity.internalServerError().body("Error reading file");
//        }
//    }
//
//    /**
//     * Lấy request path từ URL
//     */
//    private String getRequestPath(HttpServletRequest request) {
//        // Lấy path từ request
//        String requestURI = request.getRequestURI();
//
//        // Loại bỏ /local_copy prefix
//        return requestURI.replaceFirst("^/local_copy", "");
//    }
//}
