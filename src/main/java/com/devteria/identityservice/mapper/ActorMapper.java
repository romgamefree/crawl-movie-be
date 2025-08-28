package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.ActorRequest;
import com.devteria.identityservice.dto.response.ActorResponse;
import com.devteria.identityservice.entity.Actor;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ActorMapper {
//    @Mapping(target = "id", ignore = true)
    Actor toEntity(ActorRequest request);

    ActorResponse toResponse(Actor entity);

    void update(@MappingTarget Actor entity, ActorRequest request);
}


