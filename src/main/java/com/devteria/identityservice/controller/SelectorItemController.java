package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.SelectorItemRequest;
import com.devteria.identityservice.dto.response.SelectorItemResponse;
import com.devteria.identityservice.service.SelectorItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/selector-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SelectorItemController {
    SelectorItemService service;

    @PostMapping("/selector/{selectorId}")
    ApiResponse<SelectorItemResponse> create(@PathVariable String selectorId, 
                                           @RequestBody @Valid SelectorItemRequest request) {
        return ApiResponse.<SelectorItemResponse>builder()
                .result(service.create(selectorId, request))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<SelectorItemResponse> update(@PathVariable String id, 
                                           @RequestBody @Valid SelectorItemRequest request) {
        return ApiResponse.<SelectorItemResponse>builder()
                .result(service.update(id, request))
                .build();
    }

    @GetMapping("/selector/{selectorId}")
    ApiResponse<List<SelectorItemResponse>> getBySelectorId(@PathVariable String selectorId) {
        return ApiResponse.<List<SelectorItemResponse>>builder()
                .result(service.getBySelectorId(selectorId))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<SelectorItemResponse> get(@PathVariable String id) {
        return ApiResponse.<SelectorItemResponse>builder()
                .result(service.get(id))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder()
                .result("Deleted")
                .build();
    }
}
