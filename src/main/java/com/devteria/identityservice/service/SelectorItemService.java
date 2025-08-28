package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.SelectorItemRequest;
import com.devteria.identityservice.dto.response.SelectorItemResponse;
import com.devteria.identityservice.entity.CrawlSource;
import com.devteria.identityservice.entity.Selector;
import com.devteria.identityservice.entity.SelectorItem;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.SelectorItemMapper;
import com.devteria.identityservice.repository.SelectorItemRepository;
import com.devteria.identityservice.repository.SelectorRepository;
import com.devteria.identityservice.repository.CrawlSourceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SelectorItemService {
    SelectorItemRepository repository;
    SelectorRepository selectorRepository;
    CrawlSourceRepository crawlSourceRepository;
    SelectorItemMapper mapper;
    SelectorValidationService validationService;

    public SelectorItemResponse create(String selectorId, SelectorItemRequest request) {
        Selector selector = selectorRepository.findById(selectorId)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        
        SelectorItem entity = mapper.toEntity(request);
        entity.setSelector(selector);

        CrawlSource crawlSource = crawlSourceRepository.findFirstBySelectorId(selector.getId())
                .orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        String baseUrl = crawlSource.getBaseUrl();

        // Validate selector item against all CrawlSources của Selector này
        boolean ok = validationService.testSelector(
                baseUrl,
                request.getQuery(),
                request.getAttribute(),
                request.getIsList() != null ? request.getIsList() : false);

        if (!ok) {
            throw new AppException(ErrorCode.SELECTOR_NOT_MATCH);
        }
        
        return mapper.toResponse(repository.save(entity));
    }

    public SelectorItemResponse update(String id, SelectorItemRequest request) {
        SelectorItem entity = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        
        mapper.update(entity, request);
        
        // Validate selector item against all CrawlSources của Selector này
        Selector selector = entity.getSelector();
        if (selector.getCrawlSources() != null && !selector.getCrawlSources().isEmpty()) {
            boolean isValid = false;
            
            for (var crawlSource : selector.getCrawlSources()) {
                boolean ok = validationService.testSelector(
                        crawlSource.getBaseUrl(), 
                        request.getQuery(), 
                        request.getAttribute(),
                        request.getIsList() != null ? request.getIsList() : false);
                
                if (ok) {
                    isValid = true;
                    break; // Chỉ cần 1 CrawlSource hợp lệ là đủ
                }
            }
            
            if (!isValid) {
                throw new AppException(ErrorCode.SELECTOR_NOT_MATCH);
            }
        }
        
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public SelectorItemResponse get(String id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<SelectorItemResponse> getBySelectorId(String selectorId) {
        return repository.findBySelectorId(selectorId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
