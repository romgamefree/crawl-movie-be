package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.ServerDataRequest;
import com.devteria.identityservice.dto.response.ServerDataResponse;
import com.devteria.identityservice.service.ServerDataService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server-data")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerDataController {
    ServerDataService service;

    @PostMapping
    ApiResponse<ServerDataResponse> create(@RequestBody @Valid ServerDataRequest request) {
        return ApiResponse.<ServerDataResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<ServerDataResponse> update(@PathVariable String id, @RequestBody @Valid ServerDataRequest request) {
        return ApiResponse.<ServerDataResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<ServerDataResponse>> list() {
        return ApiResponse.<List<ServerDataResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<ServerDataResponse> get(@PathVariable String id) {
        return ApiResponse.<ServerDataResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


