package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Episode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EpisodeRepository extends JpaRepository<Episode, String> {
    Optional<Episode> findByMovieIdAndServerName(String movieId, String episodeServerName);
}
