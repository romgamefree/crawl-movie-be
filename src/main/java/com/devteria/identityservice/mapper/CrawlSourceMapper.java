package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.CrawlSourceRequest;
import com.devteria.identityservice.dto.response.CrawlSourceResponse;
import com.devteria.identityservice.entity.CrawlSource;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CrawlSourceMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "selector", ignore = true)
    CrawlSource toEntity(CrawlSourceRequest request);

    @Mapping(target = "selectorId", source = "selector.id")
    CrawlSourceResponse toResponse(CrawlSource entity);

    @Mapping(target = "selector", ignore = true)
    void update(@MappingTarget CrawlSource entity, CrawlSourceRequest request);
}


