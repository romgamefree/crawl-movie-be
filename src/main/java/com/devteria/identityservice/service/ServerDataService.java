package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.ServerDataRequest;
import com.devteria.identityservice.dto.response.ServerDataResponse;
import com.devteria.identityservice.entity.Episode;
import com.devteria.identityservice.entity.ServerData;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.ServerDataMapper;
import com.devteria.identityservice.repository.EpisodeRepository;
import com.devteria.identityservice.repository.ServerDataRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerDataService {
    ServerDataRepository repository;
    EpisodeRepository episodeRepository;
    ServerDataMapper mapper;

    public ServerDataResponse create(ServerDataRequest request) {
        ServerData entity = mapper.toEntity(request);
        Episode episode = episodeRepository.findById(request.getEpisodeId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        entity.setEpisode(episode);
        return mapper.toResponse(repository.save(entity));
    }

    public ServerDataResponse update(String id, ServerDataRequest request) {
        ServerData entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        if (request.getEpisodeId() != null) {
            Episode episode = episodeRepository.findById(request.getEpisodeId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
            entity.setEpisode(episode);
        }
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public ServerDataResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<ServerDataResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


