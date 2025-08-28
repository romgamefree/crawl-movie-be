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
public class TmdbResponse {
    String type; // tv
    Integer season;
    @JsonProperty("id")
    String id;
    @JsonProperty("vote_average")
    Double voteAverage;
    @JsonProperty("vote_count")
    Integer voteCount;
}
