package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.Config;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigRepository extends JpaRepository<Config, String> {
    Optional<Config> findByKey(String key);
}
