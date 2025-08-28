package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.SelectorRequest;
import com.devteria.identityservice.dto.response.SelectorResponse;
import com.devteria.identityservice.service.SelectorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/selectors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SelectorController {
    SelectorService service;

    @PostMapping
    ApiResponse<SelectorResponse> create(@RequestBody @Valid SelectorRequest request) {
        return ApiResponse.<SelectorResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<SelectorResponse> update(@PathVariable String id, @RequestBody @Valid SelectorRequest request) {
        return ApiResponse.<SelectorResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<SelectorResponse>> list() {
        return ApiResponse.<List<SelectorResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<SelectorResponse> get(@PathVariable String id) {
        return ApiResponse.<SelectorResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


