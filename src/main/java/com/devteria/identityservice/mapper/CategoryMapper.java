package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.CategoryRequest;
import com.devteria.identityservice.dto.response.CategoryResponse;
import com.devteria.identityservice.entity.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
//    @Mapping(target = "id", ignore = true)
    Category toEntity(CategoryRequest request);

    CategoryResponse toResponse(Category entity);

    void update(@MappingTarget Category entity, CategoryRequest request);
}


