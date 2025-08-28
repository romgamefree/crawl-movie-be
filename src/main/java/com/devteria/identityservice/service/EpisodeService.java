package com.devteria.identityservice.service;

import com.devteria.identityservice.constant.MovieStatus;
import com.devteria.identityservice.constant.MovieType;
import com.devteria.identityservice.dto.request.EpisodeRequest;
import com.devteria.identityservice.dto.request.MovieRequest;
import com.devteria.identityservice.dto.response.EpisodeResponse;
import com.devteria.identityservice.dto.response.MovieResponse;
import com.devteria.identityservice.entity.*;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.EpisodeMapper;
import com.devteria.identityservice.repository.EpisodeRepository;
import com.devteria.identityservice.repository.MovieRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EpisodeService {
    EpisodeRepository repository;
    MovieRepository movieRepository;
    EpisodeMapper mapper;

    public EpisodeResponse create(EpisodeRequest request) {
        Episode entity = mapper.toEntity(request);
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        entity.setMovie(movie);
        return mapper.toResponse(repository.save(entity));
    }

    public EpisodeResponse update(String id, EpisodeRequest request) {
        Episode entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        if (request.getMovieId() != null) {
            Movie movie = movieRepository.findById(request.getMovieId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
            entity.setMovie(movie);
        }
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public EpisodeResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<EpisodeResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

}


