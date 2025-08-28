package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.ActorRequest;
import com.devteria.identityservice.dto.response.ActorResponse;
import com.devteria.identityservice.entity.Actor;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.ActorMapper;
import com.devteria.identityservice.repository.ActorRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActorService {
    ActorRepository repository;
    ActorMapper mapper;

    public ActorResponse create(ActorRequest request) {
        Actor entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public ActorResponse update(String id, ActorRequest request) {
        Actor entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public ActorResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<ActorResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


