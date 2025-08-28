package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.DirectorRequest;
import com.devteria.identityservice.dto.response.DirectorResponse;
import com.devteria.identityservice.entity.Director;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.DirectorMapper;
import com.devteria.identityservice.repository.DirectorRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorService {
    DirectorRepository repository;
    DirectorMapper mapper;

    public DirectorResponse create(DirectorRequest request) {
        Director entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public DirectorResponse update(String id, DirectorRequest request) {
        Director entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public DirectorResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<DirectorResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


