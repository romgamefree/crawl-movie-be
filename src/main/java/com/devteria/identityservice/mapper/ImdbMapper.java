package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ImdbRequest;
import com.devteria.identityservice.dto.response.TmdbResponse;
import com.devteria.identityservice.entity.Imdb;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ImdbMapper {
//    @Mapping(target = "id", ignore = true)
    Imdb toEntity(ImdbRequest request);

    TmdbResponse toResponse(Imdb entity);

    void update(@MappingTarget Imdb entity, ImdbRequest request);
}


