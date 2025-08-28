package com.devteria.identityservice.dto.response;

import com.devteria.identityservice.constant.MovieStatus;
import com.devteria.identityservice.constant.MovieType;
import com.devteria.identityservice.entity.Created;
import com.devteria.identityservice.entity.Imdb;
import com.devteria.identityservice.entity.Modified;
import com.devteria.identityservice.entity.Tmdb;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieResponse {
    @JsonProperty("_id")
    String id;

    TmdbResponse tmdb;

    ImdbResponse imdb;

    CreatedResponse created;

    ModifiedResponse modified;

    String title;

    String slug;

    String originName;

    String content;

    MovieType type; // series | single

    MovieStatus status; // completed | ongoing | unknown

    String posterUrl;
    String thumbnailUrl;

    Boolean isCopyright;

    Boolean subDocquyen;

    Boolean chieurap;

    String trailerUrl;

    String time;

    String episodeCurrent;

    String episodeTotal;

    String quality; // FHD

    String lang; // Vietsub + Long tieng

    String notify;

    String showtimes;

    Integer year;

    Integer view;

    Set<String> actors;
    Set<String> directors;

    Set<CategoryResponse> categories;
    Set<CountryResponse> countries;
    Set<EpisodeResponse> episodes;
}


