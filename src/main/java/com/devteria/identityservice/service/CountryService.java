package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.CountryRequest;
import com.devteria.identityservice.dto.response.CountryResponse;
import com.devteria.identityservice.entity.Country;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.CountryMapper;
import com.devteria.identityservice.repository.CountryRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CountryService {
    CountryRepository repository;
    CountryMapper mapper;

    public CountryResponse create(CountryRequest request) {
        Country entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public CountryResponse update(String id, CountryRequest request) {
        Country entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public CountryResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<CountryResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


