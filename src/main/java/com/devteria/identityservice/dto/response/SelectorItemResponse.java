package com.devteria.identityservice.dto.response;

import lombok.*;
import com.devteria.identityservice.constant.SelectorAttribute;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SelectorItemResponse {
    private String id;
    private String selectorId;
    private String name;
    private String query;
    private String attribute;
    private String note;
    private Boolean isList;
}
