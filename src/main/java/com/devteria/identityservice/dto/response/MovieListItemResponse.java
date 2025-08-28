package com.devteria.identityservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieListItemResponse {
    @JsonProperty("tmdb")
    TmdbResponse tmdb;

    @JsonProperty("imdb")
    ImdbResponse imdb;

    @JsonProperty("modified")
    ModifiedResponse modified;

    @JsonProperty("_id")
    String id;

    @JsonProperty("name")
    String title;

    @JsonProperty("slug")
    String slug;

    @JsonProperty("origin_name")
    String originName;

    @JsonProperty("poster_url")
    String posterUrl;

    @JsonProperty("thumb_url")
    String thumbnailUrl;

    @JsonProperty("year")
    Integer year;

    @JsonProperty("status")
    String status;

    @JsonProperty("episode_current")
    String episodeCurrent;

}
