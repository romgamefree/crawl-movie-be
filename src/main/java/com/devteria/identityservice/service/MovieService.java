package com.devteria.identityservice.service;

import com.devteria.identityservice.dto.request.MovieRequest;
import com.devteria.identityservice.dto.response.*;
import com.devteria.identityservice.entity.*;
import com.devteria.identityservice.exception.AppException;
import com.devteria.identityservice.exception.ErrorCode;
import com.devteria.identityservice.mapper.MovieMapper;
import com.devteria.identityservice.repository.*;

import java.util.*;
import java.util.stream.Collectors;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MovieService {
    MovieRepository movieRepository;
    DirectorRepository directorRepository;
    ActorRepository actorRepository;
    CategoryRepository categoryRepository;
    CountryRepository countryRepository;
    MovieMapper movieMapper;

    public MovieResponse create(MovieRequest request) {
        Movie movie = movieMapper.toEntity(request);
        attachRelations(movie, request);
        movie = movieRepository.save(movie);
        return movieMapper.toResponse(movie);
    }

    public MovieResponse update(String id, MovieRequest request) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        Movie updated = movieMapper.toEntity(request);
        updated.setId(movie.getId());
        attachRelations(updated, request);
        updated = movieRepository.save(updated);
        return movieMapper.toResponse(updated);
    }

    public void delete(String id) {
        movieRepository.deleteById(id);
    }

    public MovieResponse get(String id) {
        return movieMapper.toResponse(
                movieRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND)));
    }

    public ListResponse<MovieListItemResponse> latest(int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page - 1, 0), size, Sort.by(Sort.Direction.DESC, "modifiedAt"));
        Page<Movie> p = movieRepository.findAll(pageable);
        List<MovieListItemResponse> items = p.getContent().stream().map(this::toListItem).toList();
        PaginationResponse pagination = PaginationResponse.builder()
                .totalItems(p.getTotalElements())
                .totalItemsPerPage(size)
                .currentPage(page)
                .totalPages(p.getTotalPages())
                .build();
        return ListResponse.<MovieListItemResponse>builder().items(items).pagination(pagination).build();
    }

    public MovieDetailResponse getBySlug(String slug) {
        Movie m = movieRepository.findBySlug(slug).orElseThrow(() -> new AppException(ErrorCode.DATA_NOT_FOUND));
        MovieResponse movieResponse = movieMapper.toResponse(m);
        Set<EpisodeResponse> eps = m.getEpisodes() == null ? Set.of()
                : m.getEpisodes().stream()
                .map(e -> EpisodeResponse.builder()
                        .serverName(e.getServerName())
                        .serverData(e.getServerData() == null ? Set.of()
                                : e.getServerData().stream()
                                .map(sd -> ServerDataResponse.builder()
                                        .name(sd.getName())
                                        .slug(sd.getSlug())
                                        .filename(sd.getFilename())
                                        .link_embed(sd.getLink_embed())
                                        .link_m3u8(sd.getLink_m3u8())
                                        .build())
                                .collect(Collectors.toSet()))
                        .build())
                .sorted(Comparator.comparing(EpisodeResponse::getServerName))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // giữ thứ tự

        return MovieDetailResponse.builder()
                .movie(movieResponse)
                .episodes(eps)
                .build();
    }

    private MovieListItemResponse toListItem(Movie m) {
        TmdbResponse tmdb = null;
        if (m.getTmdb() != null) {
            tmdb = TmdbResponse.builder()
                    .type(m.getTmdb().getType())
                    .season(m.getTmdb().getSeason())
                    .id(m.getTmdb().getId())
                    .voteAverage(
                            m.getTmdb().getVoteAverage() != null ? m.getTmdb().getVoteAverage().doubleValue() : null)
                    .voteCount(m.getTmdb().getVoteCount())
                    .build();
        }

        ImdbResponse imdb = null;
        if (m.getImdb() != null) {
            imdb = ImdbResponse.builder()
                    .id(m.getImdb().getId())
                    .build();
        }

        return MovieListItemResponse.builder()
                .tmdb(tmdb)
                .imdb(imdb)
                .modified(ModifiedResponse.builder().time(m.getModifiedAt())
                        .build())
                .id(m.getId())
                .title(m.getTitle())
                .slug(m.getSlug())
                .originName(m.getOriginName())
                .posterUrl(m.getPosterUrl())
                .thumbnailUrl(m.getThumbnailUrl())
                .year(m.getYear())
                .status(m.getStatus() != null ? m.getStatus().name() : null)
                .episodeCurrent(m.getEpisodeCurrent())
                .build();
    }

    private void attachRelations(Movie movie, MovieRequest request) {
        if (request.getDirectorIds() != null) {
            movie.setDirectors(new HashSet<>(directorRepository.findAllById(request.getDirectorIds())));
        }
        if (request.getActorIds() != null) {
            movie.setActors(new HashSet<>(actorRepository.findAllById(request.getActorIds())));
        }
        if (request.getCategoryIds() != null) {
            movie.setCategories(new HashSet<>(categoryRepository.findAllById(request.getCategoryIds())));
        }
        if (request.getCountryIds() != null) {
            movie.setCountries(new HashSet<>(countryRepository.findAllById(request.getCountryIds())));
        }
    }

}
