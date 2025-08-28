package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.DirectorRequest;
import com.devteria.identityservice.dto.response.DirectorResponse;
import com.devteria.identityservice.service.DirectorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DirectorController {
    DirectorService service;

    @PostMapping
    ApiResponse<DirectorResponse> create(@RequestBody @Valid DirectorRequest request) {
        return ApiResponse.<DirectorResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<DirectorResponse> update(@PathVariable String id, @RequestBody @Valid DirectorRequest request) {
        return ApiResponse.<DirectorResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<DirectorResponse>> list() {
        return ApiResponse.<List<DirectorResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<DirectorResponse> get(@PathVariable String id) {
        return ApiResponse.<DirectorResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


