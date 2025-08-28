package com.devteria.identityservice.controller;

import com.devteria.identityservice.dto.request.ApiResponse;
import com.devteria.identityservice.dto.request.MovieRequest;
import com.devteria.identityservice.dto.response.MovieResponse;
import com.devteria.identityservice.dto.response.MovieListItemResponse;
import com.devteria.identityservice.dto.response.PaginationResponse;
import com.devteria.identityservice.dto.response.ListResponse;
import com.devteria.identityservice.dto.response.MovieDetailResponse;
import com.devteria.identityservice.service.MovieService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieController {
    MovieService movieService;

    @PostMapping
    ApiResponse<MovieResponse> create(@RequestBody @Valid MovieRequest request) {
        return ApiResponse.<MovieResponse>builder().result(movieService.create(request)).build();
    }

    @PutMapping("/{id}")
    ApiResponse<MovieResponse> update(@PathVariable String id, @RequestBody @Valid MovieRequest request) {
        return ApiResponse.<MovieResponse>builder().result(movieService.update(id, request)).build();
    }

    @GetMapping("/{id}")
    ApiResponse<MovieResponse> get(@PathVariable String id) {
        return ApiResponse.<MovieResponse>builder().result(movieService.get(id)).build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable String id) {
        movieService.delete(id);
        return ApiResponse.<String>builder().result("Deleted").build();
    }

    @GetMapping("/danh-sach/phim-moi-cap-nhat")
    ListResponse<MovieListItemResponse> latest(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return movieService.latest(page, size);
    }

    @GetMapping("/phim/{slug}")
    MovieDetailResponse getBySlug(@PathVariable String slug) {
        return movieService.getBySlug(slug);
    }
}
