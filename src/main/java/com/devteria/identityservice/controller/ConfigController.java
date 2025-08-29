package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.ConfigRequest;
import com.devteria.identityservice.dto.response.ConfigResponse;
import com.devteria.identityservice.service.ConfigService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/configs")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigController {
    ConfigService service;

    @PostMapping
    ApiResponse<ConfigResponse> create(@RequestBody @Valid ConfigRequest request) {
        return ApiResponse.<ConfigResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<ConfigResponse> update(@PathVariable String id, @RequestBody @Valid ConfigRequest request) {
        return ApiResponse.<ConfigResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<ConfigResponse>> list() {
        return ApiResponse.<List<ConfigResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<ConfigResponse> get(@PathVariable String id) {
        return ApiResponse.<ConfigResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }

    @GetMapping("/by-key/{key}")
    ApiResponse<ConfigResponse> getByKey(@PathVariable String key) {
        return ApiResponse.<ConfigResponse>builder().result(service.getByKey(key)).build();
    }
}
