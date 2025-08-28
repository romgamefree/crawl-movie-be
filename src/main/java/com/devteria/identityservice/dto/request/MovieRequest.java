package com.devteria.identityservice.dto.request;

import com.devteria.identityservice.constant.MovieStatus;
import com.devteria.identityservice.constant.MovieType;
import java.util.Set;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieRequest {
    String title;
    String slug;
    String description;
    Integer releaseYear;
    Integer durationMinutes;
    String posterUrl;
    String thumbnailUrl;
    MovieType type;
    MovieStatus status;

    Set<String> directorIds;
    Set<String> actorIds;
    Set<String> categoryIds;
    Set<String> countryIds;
}


