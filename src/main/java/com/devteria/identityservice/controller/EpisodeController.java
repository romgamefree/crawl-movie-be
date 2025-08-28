package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.EpisodeRequest;
import com.devteria.identityservice.dto.response.EpisodeResponse;
import com.devteria.identityservice.service.EpisodeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/episodes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EpisodeController {
    EpisodeService service;

    @PostMapping
    ApiResponse<EpisodeResponse> create(@RequestBody @Valid EpisodeRequest request) {
        return ApiResponse.<EpisodeResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<EpisodeResponse> update(@PathVariable String id, @RequestBody @Valid EpisodeRequest request) {
        return ApiResponse.<EpisodeResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<EpisodeResponse>> list() {
        return ApiResponse.<List<EpisodeResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<EpisodeResponse> get(@PathVariable String id) {
        return ApiResponse.<EpisodeResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


