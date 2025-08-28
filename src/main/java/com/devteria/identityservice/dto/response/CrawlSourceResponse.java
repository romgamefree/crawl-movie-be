package com.devteria.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CrawlSourceResponse {
    String id;
    String code;
    String name;
    String baseUrl;
    Boolean enabled;
    String note;
    String selectorId;
}


