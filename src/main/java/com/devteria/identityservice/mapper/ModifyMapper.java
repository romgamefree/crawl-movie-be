package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ModifiedRequest;
import com.devteria.identityservice.dto.response.ModifiedResponse;
import com.devteria.identityservice.entity.Modified;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ModifyMapper {
//    @Mapping(target = "id", ignore = true)
    Modified toEntity(ModifiedRequest request);

    ModifiedResponse toResponse(Modified entity);

    void update(@MappingTarget Modified entity, ModifiedRequest request);
}


