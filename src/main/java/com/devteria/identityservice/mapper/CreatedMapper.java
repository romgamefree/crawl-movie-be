package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.CreatedRequest;
import com.devteria.identityservice.dto.response.CreatedResponse;
import com.devteria.identityservice.entity.Created;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CreatedMapper {
//    @Mapping(target = "id", ignore = true)
    Created toEntity(CreatedRequest request);

    CreatedResponse toResponse(Created entity);

    void update(@MappingTarget Created entity, CreatedRequest request);
}


