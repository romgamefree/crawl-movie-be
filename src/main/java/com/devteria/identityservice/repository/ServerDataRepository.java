package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.ServerData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerDataRepository extends JpaRepository<ServerData, String> {
}
