package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ActorRequest;
import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.response.ActorResponse;
import com.devteria.identityservice.service.ActorService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/actors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActorController {
    ActorService service;

    @PostMapping
    ApiResponse<ActorResponse> create(@RequestBody @Valid ActorRequest request) {
        return ApiResponse.<ActorResponse>builder().result(service.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<ActorResponse> update(@PathVariable String id, @RequestBody @Valid ActorRequest request) {
        return ApiResponse.<ActorResponse>builder().result(service.update(id, request)).build();
    }

    @GetMapping
    ApiResponse<List<ActorResponse>> list() {
        return ApiResponse.<List<ActorResponse>>builder().result(service.list()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<ActorResponse> get(@PathVariable String id) {
        return ApiResponse.<ActorResponse>builder().result(service.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        service.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }
}


