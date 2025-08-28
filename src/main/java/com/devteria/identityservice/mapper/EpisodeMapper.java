package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.EpisodeRequest;
import com.devteria.identityservice.dto.response.EpisodeResponse;
import com.devteria.identityservice.entity.Episode;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface EpisodeMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "movie", ignore = true)
//    @Mapping(target = "videoLinks", ignore = true)
    Episode toEntity(EpisodeRequest request);

//    @Mapping(target = "movieId", source = "movie.id")
    EpisodeResponse toResponse(Episode entity);

    @Mapping(target = "movie", ignore = true)
//    @Mapping(target = "videoLinks", ignore = true)
    void update(@MappingTarget Episode entity, EpisodeRequest request);
}


