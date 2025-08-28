package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String>, JpaSpecificationExecutor<Movie> {
    Optional<Movie> findBySlug(String slug);

    Optional<Movie> findByTitle(String title);

    List<Movie> findByThumbnailUrl(String thumbnailUrl);
}
