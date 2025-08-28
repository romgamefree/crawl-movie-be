package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.SelectorRequest;
import com.devteria.identityservice.dto.response.SelectorResponse;
import com.devteria.identityservice.entity.Selector;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SelectorMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "crawlSources", ignore = true)
//    @Mapping(target = "selectorItems", ignore = true)
    Selector toEntity(SelectorRequest request);

//    @Mapping(target = "crawlSourceIds", expression = "java(entity.getCrawlSources().stream().map(cs -> cs.getId()).toList())")
    @Mapping(target = "selectorItems", source = "selectorItems")
    SelectorResponse toResponse(Selector entity);

    @Mapping(target = "crawlSources", ignore = true)
    @Mapping(target = "selectorItems", ignore = true)
    void update(@MappingTarget Selector entity, SelectorRequest request);
}


