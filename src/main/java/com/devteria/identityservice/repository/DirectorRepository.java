package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Director;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectorRepository extends JpaRepository<Director, String> {
    Optional<Director> findByName(String name);
}


