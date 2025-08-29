package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ConfigRequest;
import com.devteria.identityservice.dto.response.ConfigResponse;
import com.devteria.identityservice.entity.Config;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ConfigMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    Config toEntity(ConfigRequest request);

    ConfigResponse toResponse(Config entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "modifiedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    void update(@MappingTarget Config entity, ConfigRequest request);
}
