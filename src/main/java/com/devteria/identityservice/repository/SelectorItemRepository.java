package com.devteria.identityservice.repository;

import com.devteria.identityservice.entity.SelectorItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SelectorItemRepository extends JpaRepository<SelectorItem, String> {
    List<SelectorItem> findBySelectorId(String selectorId);
}
