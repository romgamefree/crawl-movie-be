package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.SelectorItemRequest;
import com.devteria.identityservice.dto.response.SelectorItemResponse;
import com.devteria.identityservice.entity.SelectorItem;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SelectorItemMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "selector", ignore = true)
    SelectorItem toEntity(SelectorItemRequest request);

    @Mapping(target = "selectorId", source = "selector.id")
    SelectorItemResponse toResponse(SelectorItem entity);

    void update(@MappingTarget SelectorItem entity, SelectorItemRequest request);
}
