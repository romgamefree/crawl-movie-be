package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.DirectorRequest;
import com.devteria.identityservice.dto.response.DirectorResponse;
import com.devteria.identityservice.entity.Director;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DirectorMapper {
//    @Mapping(target = "id", ignore = true)
    Director toEntity(DirectorRequest request);

    DirectorResponse toResponse(Director entity);

    void update(@MappingTarget Director entity, DirectorRequest request);
}


