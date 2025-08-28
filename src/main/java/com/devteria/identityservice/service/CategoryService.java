package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.CategoryRequest;
import com.devteria.identityservice.dto.response.CategoryResponse;
import com.devteria.identityservice.entity.Category;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.CategoryMapper;
import com.devteria.identityservice.repository.CategoryRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {
    CategoryRepository repository;
    CategoryMapper mapper;

    public CategoryResponse create(CategoryRequest request) {
        Category entity = mapper.toEntity(request);
        return mapper.toResponse(repository.save(entity));
    }

    public CategoryResponse update(String id, CategoryRequest request) {
        Category entity = repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        mapper.update(entity, request);
        return mapper.toResponse(repository.save(entity));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public CategoryResponse get(String id) {
        return mapper.toResponse(repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY)));
    }

    public List<CategoryResponse> list() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }
}


