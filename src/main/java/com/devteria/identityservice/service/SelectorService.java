package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.SelectorRequest;
import com.devteria.identityservice.dto.response.SelectorResponse;
import com.devteria.identityservice.entity.CrawlSource;
import com.devteria.identityservice.entity.Selector;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.SelectorMapper;
import com.devteria.identityservice.repository.CrawlSourceRepository;
import com.devteria.identityservice.repository.SelectorRepository;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SelectorService {
    SelectorRepository repository;
    CrawlSourceRepository crawlSourceRepository;
    SelectorMapper mapper;

    public SelectorResponse create(SelectorRequest request) {
        Selector entity = mapper.toEntity(request);
        
        // Set CrawlSources nếu có crawlSourceIds
        if (request.getCrawlSourceIds() != null && !request.getCrawlSourceIds().isEmpty()) {
            Set<CrawlSource> crawlSources = request.getCrawlSourceIds().stream()
                    .map(id -> crawlSourceRepository.findById(id)
                            .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)))
                    .collect(java.util.stream.Collectors.toSet());
            entity.setCrawlSources(crawlSources);
        }
        
        return mapper.toResponse(repository.save(entity));
    }

    public SelectorResponse update(String id, SelectorRequest request) {
        Selector entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        
        // Set CrawlSources nếu có crawlSourceIds
        if (request.getCrawlSourceIds() != null && !request.getCrawlSourceIds().isEmpty()) {
            Set<CrawlSource> crawlSources = request.getCrawlSourceIds().stream()
                    .map(crawlSourceId -> crawlSourceRepository.findById(crawlSourceId)
                            .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)))
                    .collect(java.util.stream.Collectors.toSet());
            entity.setCrawlSources(crawlSources);
        } else {
            entity.setCrawlSources(new java.util.HashSet<>());
        }
        
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public SelectorResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<SelectorResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


