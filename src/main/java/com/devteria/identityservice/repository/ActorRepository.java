package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActorRepository extends JpaRepository<Actor, String> {
    Optional<Actor> findByName(String name);
}


