package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.CategoryRequest;
import com.devteria.identityservice.dto.response.CategoryResponse;
import com.devteria.identityservice.service.CategoryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService service;

    @PostMapping
    ApiResponse<CategoryResponse> create(@RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<CategoryResponse> update(@PathVariable String id, @RequestBody @Valid CategoryRequest request) {
        return ApiResponse.<CategoryResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.<List<CategoryResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<CategoryResponse> get(@PathVariable String id) {
        return ApiResponse.<CategoryResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


