package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ServerDataRequest;
import com.devteria.identityservice.dto.request.TmdbRequest;
import com.devteria.identityservice.dto.response.ServerDataResponse;
import com.devteria.identityservice.dto.response.TmdbResponse;
import com.devteria.identityservice.entity.ServerData;
import com.devteria.identityservice.entity.Tmdb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TmdbMapper {
//    @Mapping(target = "id", ignore = true)
    Tmdb toEntity(TmdbRequest request);

    TmdbResponse toResponse(Tmdb entity);

    void update(@MappingTarget Tmdb entity, TmdbRequest request);
}


