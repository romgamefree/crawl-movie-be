package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ServerDataRequest;
import com.devteria.identityservice.dto.response.ServerDataResponse;
import com.devteria.identityservice.entity.ServerData;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ServerDataMapper {
//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "episode", ignore = true)
    ServerData toEntity(ServerDataRequest request);

//    @Mapping(target = "episodeId", source = "episode.id")
    ServerDataResponse toResponse(ServerData entity);

    @Mapping(target = "episode", ignore = true)
    void update(@MappingTarget ServerData entity, ServerDataRequest request);
}


