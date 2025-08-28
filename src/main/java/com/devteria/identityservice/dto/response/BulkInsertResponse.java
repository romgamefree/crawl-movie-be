package com.devteria.identityservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkInsertResponse {
    private int totalSources;
    private int successCount;
    private int errorCount;
    private List<String> errorUrls;
    private String message;
    private long processingTimeMs;
}
