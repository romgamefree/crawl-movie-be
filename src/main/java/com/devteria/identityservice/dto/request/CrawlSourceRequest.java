package com.devteria.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlSourceRequest {
    String code;
    String name;
    String baseUrl;
    Boolean enabled;
    String note;
    String selectorId;
}


