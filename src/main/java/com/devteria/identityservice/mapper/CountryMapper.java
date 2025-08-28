package com.devteria.identityservice.mapper;

import com.devteria.identityservice.dto.request.CountryRequest;
import com.devteria.identityservice.dto.response.CountryResponse;
import com.devteria.identityservice.entity.Country;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CountryMapper {
//    @Mapping(target = "id", ignore = true)
    Country toEntity(CountryRequest request);

    CountryResponse toResponse(Country entity);

    void update(@MappingTarget Country entity, CountryRequest request);
}


