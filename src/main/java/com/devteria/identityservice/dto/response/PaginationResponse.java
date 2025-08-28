package com.devteria.identityservice.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginationResponse {
    long totalItems;
    int totalItemsPerPage;
    int currentPage;
    int totalPages;
}
