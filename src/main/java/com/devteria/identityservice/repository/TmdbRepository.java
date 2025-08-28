package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Tmdb;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TmdbRepository extends JpaRepository<Tmdb, String> {}


