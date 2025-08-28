package com.devteria.identityservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieDetailResponse {
    @Builder.Default
    boolean status = true;

    @Builder.Default
    String msg = "done";

    @JsonProperty("movie")
    MovieResponse movie;

    @JsonProperty("episodes")
    Set<EpisodeResponse> episodes;
}
