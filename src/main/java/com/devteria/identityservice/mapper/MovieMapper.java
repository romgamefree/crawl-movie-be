package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.MovieRequest;
import com.devteria.identityservice.dto.response.MovieResponse;
import com.devteria.identityservice.entity.*;

import java.util.Set;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = { CreatedMapper.class, ModifyMapper.class, CategoryMapper.class,
        CountryMapper.class, EpisodeMapper.class })
public interface MovieMapper {

    // @Mapping(target = "id", ignore = true)
    // @Mapping(target = "directors", ignore = true)
    // @Mapping(target = "actors", ignore = true)
    // @Mapping(target = "categories", ignore = true)
    // @Mapping(target = "countries", ignore = true)
    // @Mapping(target = "episodes", ignore = true)
    Movie toEntity(MovieRequest request);

    @Mapping(target = "directors", expression = "java(mapNames(movie.getDirectors()))")
    @Mapping(target = "actors", expression = "java(mapNames(movie.getActors()))")
    @Mapping(target = "created.time", source = "createdAt")
    @Mapping(target = "modified.time", source = "modifiedAt")
    MovieResponse toResponse(Movie movie);

    default Set<String> mapNames(Set<? extends Object> entities) {
        if (entities == null)
            return java.util.Set.of();
        return entities.stream()
                .map(e -> {
                    if (e instanceof Director d)
                        return d.getName();
                    if (e instanceof Actor a)
                        return a.getName();
                    if (e instanceof Category c)
                        return c.getName();
                    if (e instanceof Country c2)
                        return c2.getName();
                    return null;
                })
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }

}
