package com.devteria.identityservice.dto.request;

import lombok.*;
import com.devteria.identityservice.constant.SelectorAttribute;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectorItemRequest {
    private String name;
    private String query;
    private String attribute;
    private String note;
    
    /**
     * Nếu true: lấy tất cả kết quả (selectAll)
     * Nếu false: chỉ lấy phần tử đầu tiên (selectFirst)
     */
    @Builder.Default
    private Boolean isList = false;
}
