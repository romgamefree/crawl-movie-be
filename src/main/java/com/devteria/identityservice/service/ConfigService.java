package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.ConfigRequest;
import com.devteria.identityservice.dto.response.ConfigResponse;
import com.devteria.identityservice.entity.Config;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.ConfigMapper;
import com.devteria.identityservice.repository.ConfigRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigService {
    ConfigRepository repository;
    ConfigMapper mapper;

    public ConfigResponse create(ConfigRequest request) {
        Config entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public ConfigResponse update(String id, ConfigRequest request) {
        Config entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public ConfigResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND)));
    }

    public List<ConfigResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    public ConfigResponse getByKey(String key) {
        return mapper
                .toResponse(repository.findByKey(key).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND)));
    }
}
