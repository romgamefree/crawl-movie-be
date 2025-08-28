package com.devteria.identityservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SitemapCrawlRequest {
    @NotBlank(message = "Sitemap URL không được để trống")
    private String sitemapUrl;
    
    @NotNull(message = "Selector ID không được để trống")
    private String selectorId;
}
