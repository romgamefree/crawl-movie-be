package com.devteria.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class Config extends BaseEntity {

    @Column(name = "config_key", unique = true, nullable = false)
    String key;

    @Column(name = "config_value", columnDefinition = "TEXT")
    String value;

    @Column(name = "config_type")
    String type;

    @Column(columnDefinition = "TEXT")
    String description;
}
