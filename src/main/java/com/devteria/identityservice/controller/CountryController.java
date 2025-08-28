package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.CountryRequest;
import com.devteria.identityservice.dto.response.CountryResponse;
import com.devteria.identityservice.service.CountryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/countries")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CountryController {
    CountryService service;

    @PostMapping
    ApiResponse<CountryResponse> create(@RequestBody @Valid CountryRequest request) {
        return ApiResponse.<CountryResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<CountryResponse> update(@PathVariable String id, @RequestBody @Valid CountryRequest request) {
        return ApiResponse.<CountryResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<CountryResponse>> list() {
        return ApiResponse.<List<CountryResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<CountryResponse> get(@PathVariable String id) {
        return ApiResponse.<CountryResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


