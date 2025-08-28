package com.devteria.identityservice.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SelectorRequest {
    List<String> crawlSourceIds;
    String name;
    String note;
}


